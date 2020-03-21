package spendreport;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.table.api.Tumble;
import org.apache.flink.table.api.java.BatchTableEnvironment;
import org.apache.flink.walkthrough.common.table.SpendReportTableSink;
import org.apache.flink.walkthrough.common.table.BoundedTransactionTableSource;
import org.apache.flink.walkthrough.common.table.TruncateDateToHour;

/**
 * Skeleton code for the table walkthrough
 */
public class SpendReport {
  public static void main(String[] args) throws Exception {
    ExecutionEnvironment env   = ExecutionEnvironment.getExecutionEnvironment();
    BatchTableEnvironment tEnv = BatchTableEnvironment.create(env);

    tEnv.registerTableSource("transactions", new BoundedTransactionTableSource());
    tEnv.registerTableSink("spend_report", new SpendReportTableSink());
    tEnv.registerFunction("truncateDateToHour", new TruncateDateToHour());

//    tEnv
//      .scan("transactions")
//      .insertInto("spend_report");



//    tEnv
//        .scan("transactions")
//        .select("accountId, timestamp.truncateDateToHour as timestamp, amount")
//        .groupBy("accountId, timestamp")
//        .select("accountId, timestamp, amount.sum as total")
//        .insertInto("spend_report");


    // 时间滚动窗口函数
    tEnv
        .scan("transactions")
        .window(Tumble.over("1.hour").on("timestamp").as("w"))
        .groupBy("accountId, w")
        .select("accountId, w.start as timestamp, amount.sum")
        .insertInto("spend_report");


    env.execute("Spend Report");
  }
}
