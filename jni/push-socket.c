#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <netdb.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <fcntl.h>
#include <netinet/tcp.h>
#define MAXDATASIZE 5000


jint Java_com_baidu_android_pushservice_jni_PushSocket_createSocket(JNIEnv *env, jobject obj,
		jint idle, jint interval, jint count, jstring server, jint port) {
	int sockfd, sendbytes;
	char buf[MAXDATASIZE];
	struct hostent *host;
	struct sockaddr_in serv_addr;

	//地址解析函数
	const char* serverStr;
	serverStr = (*env)->GetStringUTFChars(env, server, 0);
	host = gethostbyname(serverStr);
	(*env)->ReleaseStringUTFChars(env, server, serverStr);
	if (host == NULL) {
		return -1;
	}

	//创建socket
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd == -1) {
		return -1;
	}

	//设置sockaddr_in 结构体中相关参数
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(port);
	serv_addr.sin_addr = *((struct in_addr *) host->h_addr);
	bzero(&(serv_addr.sin_zero), 8);
	//调用connect函数主动发起对服务器端的连接
	if (connect(sockfd, (struct sockaddr *) &serv_addr, sizeof(struct sockaddr))
			== -1) {
		return -1;
	}

	int keepalive = 1; // 开启keepalive属性
	int keepidle = idle; // 如该连接在300秒内没有任何数据往来,则进行探测
	int keepinterval = interval; // 探测时发包的时间间隔为30 秒
	int keepcount = count; // 探测尝试的次数.如果第1次探测包就收到响应了,则后2次的不再发.
	setsockopt(sockfd, SOL_SOCKET, SO_KEEPALIVE, (void *) &keepalive,
			sizeof(keepalive));
	setsockopt(sockfd, SOL_TCP, TCP_KEEPIDLE, (void*) &keepidle,
			sizeof(keepidle));
	setsockopt(sockfd, SOL_TCP, TCP_KEEPINTVL, (void *) &keepinterval,
			sizeof(keepinterval));
	setsockopt(sockfd, SOL_TCP, TCP_KEEPCNT, (void *) &keepcount,
			sizeof(keepcount));

	return sockfd;
}

jint Java_com_baidu_android_pushservice_jni_PushSocket_sendMsg(JNIEnv *env, jobject obj,
		jint sockfd, jbyteArray strIn, jint arraylen) {
	char* data = (char*) (*env)->GetByteArrayElements(env, strIn, 0);
	int revBytes = send(sockfd, data, arraylen, 0);
	return revBytes;
}

jint Java_com_baidu_android_pushservice_jni_PushSocket_sendHeartbeat(JNIEnv *env, jobject obj,
		jint sockfd) {
	char data[2] = { 5, 0 };
	int revBytes = send(sockfd, data, 2, 0);
	return revBytes;
}

jbyteArray Java_com_baidu_android_pushservice_jni_PushSocket_rcvMsg(JNIEnv *env, jobject obj,
		jint sockfd) {
	char buf[65535];
	int rcvlen = recv(sockfd, buf, 65535, 0);
	if (rcvlen == -1) {
		return NULL;
	}
	jbyteArray bytearray = (*env)->NewByteArray(env, rcvlen);
	(*env)->SetByteArrayRegion(env, bytearray, 0, rcvlen, buf);
	return bytearray;

}

jint Java_com_baidu_android_pushservice_jni_PushSocket_getLastSocketError() {
	return errno;
}

jint Java_com_baidu_android_pushservice_jni_PushSocket_closeSocket(JNIEnv *env, jobject obj,jint sockfd)
{
	shutdown(sockfd, SHUT_RDWR);
	return close(sockfd);
}
