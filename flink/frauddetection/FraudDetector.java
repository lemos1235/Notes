package spendreport;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.walkthrough.common.entity.Alert;
import org.apache.flink.walkthrough.common.entity.Transaction;


/**
 * Skeleton code for implementing a fraud detector.
 */
public class FraudDetector extends KeyedProcessFunction<Long, Transaction, Alert> {

  private static final long serialVersionUID = 1L;

    private transient ValueState<Boolean> flagState;
  private transient ValueState<Long> timerState;

  private static final double SMALL_AMOUNT = 1.00;
  private static final double LARGE_AMOUNT = 500.00;
  private static final long ONE_MINUTE = 60 * 1000;

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
                "flag",
                Types.BOOLEAN);
        // get state for current key
        flagState = getRuntimeContext().getState(flagDescriptor);

    ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
        "timer-state",
        Types.LONG);
    timerState = getRuntimeContext().getState(timerDescriptor);
    }

  /**
   *
   当标记状态被设置为 true 时，设置一个在当前时间一分钟后触发的定时器。
   当定时器被触发时，重置标记状态。
   当标记状态被重置时，删除定时器。
   */
    @Override
  public void processElement(
      Transaction transaction,
      Context context,
      Collector<Alert> collector) throws Exception {

    // Get the current state for the current key
    Boolean lastTransactionWasSmall = flagState.value();

    // Check if the flag is set
    if (lastTransactionWasSmall != null) {
      if (transaction.getAmount() > LARGE_AMOUNT) {
        // Output an alert downstream
        Alert alert = new Alert();
        alert.setId(transaction.getAccountId());

        collector.collect(alert);
      }

      // Clean up our state
      cleanUp(context);
    }

    if (transaction.getAmount() < SMALL_AMOUNT) {
      // Set the flag to true
      flagState.update(true);

      // set the timer and timer state
      long timer = context.timerService().currentProcessingTime() + ONE_MINUTE;
      context.timerService().registerProcessingTimeTimer(timer);
      timerState.update(timer);
    }
  }

  @Override
  public void onTimer(long timestamp, OnTimerContext ctx, Collector<Alert> out) throws Exception {
    // remove flag after 1 minute
    timerState.clear();
    flagState.clear();
  }



  private void cleanUp(Context ctx) throws Exception {
    // delete timer
    Long timer = timerState.value();
    ctx.timerService().deleteProcessingTimeTimer(timer);

    // clean up all state
    timerState.clear();
    flagState.clear();
  }

}