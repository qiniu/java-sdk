##七牛设置notifyURL没有收到回调

在使用七牛进行数据处理时用户可以使用<persistentId>来主动查询持久化处理的执行状态，具体查询方法是发送一个Get请求：http://api.qiniu.com/status/get/prefop?id=
可以参考：
http://developer.qiniu.com/docs/v6/api/reference/fop/pfop/prefop.html 

但是调用查询的成本比较高，还得起脚本实时去遍历查询，如果能回调，就可以省掉这个工作了。

正好七牛这边针对上传预转持续化和触发持续化分别提供persistentNotifyUrl和notifyUrl讲处理结果POST到用户业务服务器，用户那边设置解析打印出处理结果就可以了。

那么问题来了，有的用户在使用过程中出现接受不到处理的结果，这种情况一般都是用户那边自己的问题，那应该怎样处理呢？
1.检查下用户设置的persistentNotifyUrl以及notifyUrl，必须是公网上可以正常进行POST请求并能响应HTTP/1.1 200 OK的有效URL，可以使用curl访问下看是否满足这个条件。
另外，七牛这边发送body格式为Content-Type为"application/json"的POST请求，用户回调服务器需要按照读取流的形式读取请求的body才能获取。

2.如果第一个条件满足的情况，我们可以检测下用户后端设定的接收回调处理的程序是否是正常的，对此，我们可以主动POST一个数据给用户的回调服务器：
eg：curl -vX POST "URL" -d "name=123.jpg"
用户那边如果能够正常打印出该内容，说明用户的接收程序是没有问题的。

3.如果以上条件都没有问题的情况下，应该是用户持续化处理本身的代码是有问题的，应该是用户设置的persistentNotifyUrl或者notifyurl没有设置成功，这个时候我们可以让用户在程序里面调试打印下这个URL的值，或者提供下返回的persistentID我们可以请求下获得ReqID然后在日志机上查询下是否是正确的，比如，之前查到的一个结果如下：
url.Values{"notifyURL":[]string{""}, "force":[]string{""}
这就明显看出用户设置的notifyURL是没有传进去的。

这时，我们可以让用户提供下代码，检查下用户代码参数设置是否是有问题的，因为，**不同的语言对于notifyURL参数的写法是有问题的，比如java里面写法是notifyURL，而PHP里面该字段是notifyUrl**，经排查果然，用户用的是PHP语言，但是里面设置notifyURL字段应该是：
$notifyUrl = 'http://notify.fake.com';
但用户设置的是$notifyURL，参数设置是有问题的

另外，可以参考java中Servlet回调处理的代码：

```
public class notify extends HttpServlet {

	public notify() {
		super();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		 response.setContentType("text/html");
	        response.setCharacterEncoding("gb2312");
	        PrintWriter out = response.getWriter();       
	      

	       request.getInputStream();
	       
	       String line="";
	       BufferedReader br=new BufferedReader(new InputStreamReader(   
	    		   request.getInputStream())); 
	    		   StringBuilder sb = new StringBuilder();
	    		   while((line = br.readLine())!=null){
	    		    sb.append(line);
	    		    }        
	    	       System.out.println( sb);    
	       
	}
}
```

