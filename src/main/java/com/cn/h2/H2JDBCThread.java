package com.cn.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2JDBCThread implements Runnable{
	private Logger logger = LoggerFactory.getLogger(H2JDBCThread.class);

	private String name;
	public H2JDBCThread(String name){
		this.name = name;
	}
	@Override
	public void run(){
		long ll_1 = System.currentTimeMillis();
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection("jdbc:h2:file:/home/h2/hyx;AUTO_SERVER=FALSE;CACHE_SIZE=102400"); //本地文件
			//conn.setAutoCommit(true);
			long ll_2 = System.currentTimeMillis(); //
			logger.debug("current thread "+name+": 创建数据库并连接成功,耗时【" + (ll_2 - ll_1) + "】"); //【" + str_dbName + "】
			//////////////////////////////////////
			long ll_3 = System.currentTimeMillis();
			for(int i=0;i<100000;i++) {
				int max_id = (int) (Math.random() * 100000); //
				stmt = conn.createStatement(); //
				String str_sql = "select * from testtable where id ='"+max_id+"'";
				//System.err.println("查询SQL【" + str_sql + "】"); //
				ResultSet rs = stmt.executeQuery(str_sql); //
				while (rs.next()) {
					//System.out.println("id【" + rs.getString("id") + "】name【" + rs.getString("name") + "】");
				}
				rs.close();
			}
			long ll_4 = System.currentTimeMillis(); //
			logger.debug("current thread "+name+": 查询100000条,数据耗时【" + (ll_4 - ll_3) + "】"); //
			//////////////////////////////////////
			long ll_5 = System.currentTimeMillis();
			stmt.close();
			conn.close();
			long ll_6 = System.currentTimeMillis(); //
			logger.debug("current thread "+name+": 关闭数据库连接耗时【" + (ll_6 - ll_5) + "】"); //
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 多线程查询
	 * @throws Exception
	 */
	public static void main(String[] args){
		try {
			createConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 200, TimeUnit.MINUTES,
				new LinkedBlockingQueue<Runnable>());
		while(true) {
			if(threadPoolExecutor.getActiveCount() != 10) {
				threadPoolExecutor.execute(new H2JDBCThread(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()).toString()));
			}
		}
	}
	
	
	
	
	
	
	private static void createConnection() throws Exception {
		Connection conn = null;
		long ll_1 = System.currentTimeMillis();

		Class.forName("org.h2.Driver");
		conn = DriverManager.getConnection("jdbc:h2:file:/home/h2/hyx;AUTO_SERVER=FALSE;CACHE_SIZE=102400"); //本地文件
		long ll_2 = System.currentTimeMillis(); //
		System.out.println("创建数据库并连接成功,耗时【" + (ll_2 - ll_1) + "】"); //【" + str_dbName + "】
		//建表
		long ll_3 = System.currentTimeMillis();
		Statement stmt1 = conn.createStatement(); //
		stmt1.executeUpdate("create table testtable(id varchar(50),name varchar(128))");
		stmt1.executeUpdate("create index in_testtable_1 on testtable(id)");
		stmt1.close();
		long ll_4 = System.currentTimeMillis(); //
		System.out.println("建表成功,耗时【" + (ll_4 - ll_3) + "】"); //
		//插入
		long ll_5 = System.currentTimeMillis();
		Statement stmt = conn.createStatement(); //
		int li_count = 300000; //插入1万条数据是20秒左右,3万条是1分钟左右
		for (int i = 1; i <= li_count; i++) {
			stmt.executeUpdate("insert into testtable (id,name) values ('" + i + "','老徐到此一游" + i + "')");
			//conn.commit();
		}
		stmt.close();
		long ll_6 = System.currentTimeMillis(); //
		System.out.println("插入[" + li_count + "]条数据耗时【" + (ll_6 - ll_5) + "】"); //

		conn.close(); //
	}


	
}
