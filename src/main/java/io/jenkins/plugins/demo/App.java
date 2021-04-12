/**
 *
 * Transform remoting.jar as below using javassist
 *
 * public class EngineListenerSplitter implements EngineListener {
 *   ...
 *   @Override
 *   public void status(String msg) {
 *       for (EngineListener l : listeners) {
 *           l.status(msg);
 *       }
 *   }
 *   ...
 * }
 *
 * ↓
 *
 * public class EngineListenerSplitter implements EngineListener {
 *   ...
 *   @Override
 *   public void status(String msg) {
 *       io.jenlins.plugins.demo.App.status(msg); // insert this line
 *       for (EngineListener l : listeners) {
 *           l.status(msg);
 *       }
 *   }
 *   ...
 * }
 *
 */


package io.jenkins.plugins.demo;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Gauge;


public class App {
  private static final int PORT = 9000;
  private static boolean isConnected = false;

  // Invoke this method before calling main class of agent.jar
  public static void premain(String agentArgs, Instrumentation inst) throws IOException {
    inst.addTransformer(
      new BeforeInsertTransformer(
        "hudson.remoting.EngineListenerSplitter",
        "status",
        "io.jenkins.plugins.demo.App.status(msg);"
      )
    );

    MetricRegistry metrics = new MetricRegistry();
    metrics.register("remote.connection.isConnected", new Gauge<Boolean>(){
      @Override
      public Boolean getValue() {
        return isConnected;
      }
    });

    new MetricsServer(PORT, metrics).start();
  }

  /**
   * This method will be called every time EngineListenerSplitter#status method is called
   */
  public static void status(String msg) {
    if (msg.equals("Connected")) {
      isConnected = true;
    } else if (msg.equals("Terminated")) {
      isConnected = false;
    }
  }
}
