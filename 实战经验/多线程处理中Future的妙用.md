# 多线程处理中Future的妙用

java 中Future是一个未来对象，里面保存这线程处理结果，它像一个提货凭证，拿着它你可以随时去提取结果。在两种情况下，离开Future几乎很难办。一种情况是拆分订单，比如你的应用收到一个批量订单，此时如果要求最快的处理订单，那么需要并发处理，并发的结果如果收集，这个问题如果自己去编程将非常繁琐，此时可以使用CompletionService解决这个问题。CompletionService将Future收集到一个队列里，可以按结果处理完成的先后顺序进队。另外一种情况是，如果你需要并发去查询一些东西（比如爬虫），并发查询只要有一个结果返回，你就认为查询到了，并且结束查询，这时也需要用CompletionService和Future来解决。直接上代码更直观：

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
 
public class CompletionServiceTest {
     
    static int numThread =100;
    static ExecutorService executor = Executors.newFixedThreadPool(numThread);
 
    public static void main(String[] args) throws Exception{
        //data表示批量任务
        int[] data =new int[100];
        for(int i=1;i<100000;i++){
            int idx =i % 100;
            data[idx] =i;
            if(i%100==0){
                testCompletionService(data);
                data =new int[100];
            }
        }
    }
     
    private static void testCompletionService(int [] data) throws Exception{       
        CompletionService<Object> ecs = new ExecutorCompletionService<Object>(executor);
        for(int i=0;i<data.length;i++){
            final Integer t=data[i];
            ecs.submit(new Callable<Object>() {
                public Object call() {
                    try {
                        Thread.sleep(new Random().nextInt(1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return t;
                }
            });
        }
        //CompletionService会按处理完后顺序返回结果
        List<Object> res =new ArrayList<Object>();
        for(int i = 0;i<data.length;i++ ){
            Future<Object> f = ecs.take();
            res.add(f.get());
        }              
        System.out.println(Thread.currentThread().getName()+":"+res);
    }
     
    private static void testBasicFuture(int [] data) throws Exception{     
        List<Future<Object>> res =new ArrayList<Future<Object>>();
        for(int i=0;i<data.length;i++){
            final Integer t=data[i];
            Future<Object> future=executor.submit(new Callable<Object>() {
                public Object call() {
                    return t;
                }
            });
            res.add(future);
        }      
         
        for(int i = 0;i<res.size();i++ ){
            Future<Object> f = res.get(i);
            Object rObject =f.get();
            System.out.print(":"+rObject);
        }              
        System.out.println("LN");
    }
}
```

