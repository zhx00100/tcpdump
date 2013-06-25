#include <stdio.h>
#include <stdlib.h>

//int main()
//{
//	int ret;
//
//	//设置行缓冲模式
//	setvbuf(stdout, (char *)NULL, _IONBF, 0);
//
////	int pid = fork();
//
//	printf("%s\n", "开始getevent！");
//	ret = system("getevent -t -q");
//
//	fflush(stdout);
//
//	printf("\n调用结果：%d\n", ret);
//
//	ret = ( ret >> 8 );
//
//	printf("\n运行结果：%d\n", ret);
//
//	printf("fflush(stdout);\n");
//
//
//	printf("%s\n", "结束getevent！");
//
//
//
//}

int main()
{
    FILE   *stream;
    FILE    *wstream;
    char   buf[1024];

    memset( buf, '/0', sizeof(buf) );//初始化buf,以免后面写如乱码到文件中



    stream = popen( /*"ls -l"*/"getevent -t -q", "r" ); //将“ls －l”命令的输出 通过管道读取（“r”参数）到FILE* stream
    //设置行缓冲模式
    setvbuf(stream, (char *)NULL, _IONBF, 0);

    wstream = fopen( "/sdcard/zhangxin/test_popen.txt", "w+"); //新建一个可写的文件

    while (1) {
    fread( buf, sizeof(char), sizeof(buf),  stream);  //将刚刚FILE* stream的数据流读取到buf中
    fwrite( buf, 1, sizeof(buf), wstream );//将buf中的数据写到FILE    *wstream对应的流中，也是写到文件中
    }

    pclose( stream );
    fclose( wstream );

    return 0;
}
