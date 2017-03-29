# java shell命令工具类

```java
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
  java shell命令工具类 
 javac JavaShellUtil.java -encoding utf-8
 */
public class JavaShellUtil {

    public static  String lineseparator=System.getProperty("line.separator");
    public static  String COMMAND_SH       = "sh";
    public static  String COMMAND_EXIT     = "exit\n";
    public static  String COMMAND_LINE_END = "\n";

    static {
        if(System.getProperty("os.name").toUpperCase().indexOf("WINDOWS")!=-1)
        {
            System.out.println("window");
            COMMAND_SH="cmd";
        }else{
            System.out.println("unix");
        }

    }
    public static void main(String[] args) {

        System.out.println(JavaShellUtil.execCommand("dir").toString());
        System.out.println(JavaShellUtil.execCommand("ls -l").toString());
        //System.out.println(JavaShellUtil.execCommand("ping www.baidu.com").toString());
        System.out.println(JavaShellUtil.execCommand("aapt v").toString());
        System.out.println(JavaShellUtil.execCommand("aapt.exe").toString());

    }

    public static CommandResult execCommand(String command) {
        return execCommand(new String[] {command}, true);
    }

    public static CommandResult execCommand(String command,  boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isNeedResultMsg);
    }

    public static CommandResult execCommand(List<String> commands, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isNeedResultMsg);
    }

    /**
     * execute shell commands
     * {@link CommandResult#result} is -1, there maybe some excepiton.
     *
     * @param commands     command array
     * @param needResponse whether need result msg
     */
    public static CommandResult execCommand(String[] commands, final boolean needResponse) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, "空命令");
        }

        Process process = null;

       final StringBuilder successMsg  = new StringBuilder();
        final StringBuilder errorMsg = new StringBuilder();

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            final  BufferedReader successResult  = new BufferedReader(new InputStreamReader(process.getInputStream()));
           final   BufferedReader errorResult =  new BufferedReader(new InputStreamReader(process.getErrorStream()));

            //此处会写文档
            new Thread(new Runnable() {
                public void run() {

                    try {
                        if (needResponse) {
                            String s;
                            while ((s = successResult.readLine()) != null) {
                                successMsg.append(s);
                                successMsg.append(lineseparator);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            //启动两个线程,解决process.waitFor()阻塞问题
            new Thread(new Runnable() {
                public void run() {

                    try {
                        if (needResponse) {
                            String s;
                            while ((s = errorResult.readLine()) != null) {
                                errorMsg.append(s);
                                errorMsg.append(lineseparator);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            result = process.waitFor();
            if (errorResult != null) {
                errorResult.close();
            }
            if (successResult != null) {
                successResult.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }

        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }

    public static class CommandResult {

        public int    result;
        public String responseMsg;
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String responseMsg, String errorMsg) {
            this.result = result;
            this.responseMsg = responseMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "CommandResult{" +
                    "errorMsg='" + errorMsg + '\'' +
                    ", result=" + result +
                    ", responseMsg='" + responseMsg + '\'' +
                    '}';
        }
    }
}
```




### Java中Process和Runtime()使用，以及调用cmd命令阻塞在process.waitfor( )的问题解决



在java中调用php程序，由于有很多控制台输出，导致一直阻塞在process.waitfor( )，只有强制终止java程序后，结果文件才会输出。根据下面两个博客内容成功解决。

用Java编写应用时，有时需要在程序中调用另一个现成的可执行程序或系统命令，这时可以通过组合使用Java提供的Runtime类和Process类的方法实现。下面是一种比较典型的程序模式：
```java
　　Process process = Runtime.getRuntime().exec("p.exe");

　　process.waitfor( );
```
在上面的程序中，第一行的“p.exe”是要执行的程序名；Runtime.getRuntime()返回当前应用程序的Runtime对象，该对象的exec()方法指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例。通过Process可以控制该子进程的执行或获取该子进程的信息。第二条语句的目的等待子进程完成再往下执行。但在windows平台上，如果处理不当，有时并不能得到预期的结果。下面是笔者在实际编程中总结的几种需要注意的情况：

　　1、执行DOS的内部命令如果要执行一条DOS内部命令，有两种方法。一种方法是把命令解释器包含在exec()的参数中。例如，执行dir命令，在NT上，可写成exec ("cmd.exe /c dir")，在windows 95/98下，可写成“command.exe/c dir”，其中参数“/c”表示命令执行后关闭Dos立即关闭窗口。另一种方法是，把内部命令放在一个批命令my_dir.bat文件中，在Java程序中写成exec("my_dir.bat")。如果仅仅写成exec("dir")，Java虚拟机则会报运行时错误。前一种方法要保证程序的可移植性，需要在程序中读取运行的操作系统平台，以调用不同的命令解释器。后一种方法则不需要做更多的处理。

　　　2、打开一个不可执行的文件打开一个不可执行的文件，但该文件存在关联的应用程序，则可以有两种方式。以打开一个word文档a.doc文件为例，Java中可以有以下两种写法：
```java
exec("start a.doc");

exec(" c:\\Program Files\\MicrosoftOffice\\office winword.exe a.doc");
```
显然，前一种方法更为简捷方便。

　　　3、**执行一个有标准输出的DOS可执行程序在windows 平台上，运行被调用程序的DOS窗口在程序执行完毕后往往并不会自动关闭，从而导致Java应用程序阻塞在waitfor( )。导致该现象的一个可能的原因是，该可执行程序的标准输出比较多，而运行窗口的标准输出缓冲区不够大。解决的办法是，利用Java提供的Process 类提供的方法让Java虚拟机截获被调用程序的DOS运行窗口的标准输出，在waitfor()命令之前读出窗口的标准输出缓冲区中的内容。**

一段典型的程序如下：

```java
String str;

Process process =Runtime.getRuntime().exec("cmd /c dir windows");

BufferedReader bufferedReader = newBufferedReader( new InputStreamReader(process.getInputStream()));

while ( (str=bufferedReader.readLine()) !=null) System.out.println(str); 　

process.waitfor(); 
```
示例这里换成
```java
public static boolean  resize(String   pic,String   picTo,int width,int height)  {

       boolean result = true;

        String cmd = "cmd /c  convert -sample " + width + "x" + height + "   "" + pic + """ +"   "" + picTo+""";

        log.debug(cmd);

       try {

            Process process = Runtime.getRuntime().exec(cmd);

           if (process.getErrorStream().read() != -1) {

                 result = false;

                 process.destroy();

            }

        } catch (IOException e) {

            log.debug("creat icon pic fail!" + e);

           result = false;

       }

       /*BufferedReader bufferedReader = new BufferedReader( newInputStreamReader(process.getInputStream());

        while ( (str=bufferedReader.readLine()) != null)System.out.println(str); 　 */

       return result;

    }
```

我使用上面的程序处理不好使。然后通过搜索相关文章看到了如下内容。问题被解决。^-^

```java

Process process = Runtime.getRuntime.exec(cmd); // 执行调用命令

InputStream is = process.getInputStream(); // 获取对应进程的输出流
BufferedReader br = new Buffered(new InputStreamReader(is)); // 缓冲读入
StringBuilder buf = new StringBuilder(); // 保存对应进程的输出结果流
String line = null;
while((line = br.readLine()) != null) buf.append(line); // 循环等待进程结束
System.out.println("ffmpeg输出内容为：" + buf);
……
```
​    本来一般都是这样来调用程序并获取进程的输出流的，但是我在windows上执行这样的调用的时候却总是在while那里被堵塞了，结果造成ffmpeg程序在执行了一会后不再执行，这里从官方的参考文档中我们可以看到这是由于缓冲区的问题，由于java进程没有清空ffmpeg程序写到缓冲区的内容，结果导致ffmpeg程序一直在等待。在网上也查找了很多这样的问题，不过说的都是使用单独的线程来进行控制，我也尝试过很多网是所说的方法，可一直没起什么作用。下面就是我的解决方法了，注意到上述代码中的红色部分了么？这里就是关键，我把它改成如下结果就可以正常运行了。
```java
InputStream is = process.getErrorStream(); // 获取ffmpeg进程的输出流
```
​    注意到没？我把它改成获取错误流这样进程就不会被堵塞了，而我之前一直想的是同样的命令我手动调用的时候可以完成，而java调用却总是完成不了，一直认为是getInputStream的缓冲区没有被清空，不过问题确实是缓冲区的内容没有被清空，但不是getInputStream的，而是getErrorStream的缓冲区，这样问题就得到解决了。所以我们在遇到java调用外部程序而导致线程阻塞的时候，可以考虑使用两个线程来同时清空process获取的两个输入流，如下这段程序：

```java
……

  Process p = Runtime.getRuntime().exec("php.exe test.php");

      //Process p = Runtime.getRuntime().exec("cmd.exe /c dir");

         final InputStream is1 = p.getInputStream();

         new Thread(new Runnable() {

             public void run() {

                 BufferedReader br = new BufferedReader(new InputStreamReader(is1));

                 try{

                 while(br.readLine() != null) ;

                 }

                 catch(Exception e) {

            e.printStackTrace();

                 }

             }

         }).start(); // 启动单独的线程来清空p.getInputStream()的缓冲区

         InputStream is2 = p.getErrorStream();

         BufferedReader br2 = new BufferedReader(new InputStreamReader(is2)); 

         StringBuilder buf = new StringBuilder(); // 保存输出结果流

         String line = null;

         while((line = br2.readLine()) != null) buf.append(line); // 

         System.out.println("输出结果为：" + buf);

……
```
​    通过这样我们使用一个线程来读取process.getInputStream()的输出流，使用另外一个线程来获取process.getErrorStream()的输出流，这样我们就可以保证缓冲区得到及时的清空而不担心线程被阻塞了。当然根据需要你也可以保留process.getInputStream()流中的内容，这个就看调用的程序的处理了。

假如源码内发现用了大量System.err.print，需要使用getErrorStream()捕捉！关于System.err和System.out的区别，可以参考别的日志。这两个流走的是不同的管道。所以需要分别捕捉。