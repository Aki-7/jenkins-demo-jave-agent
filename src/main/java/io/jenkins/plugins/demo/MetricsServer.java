package io.jenkins.plugins.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Collect metrics and serve them.
 */
public class MetricsServer implements HttpHandler {
  private final HttpServer server;
  private final MetricRegistry metrics;
  private final int port;

  public MetricsServer(int port, MetricRegistry metrics) throws IOException{
    this.metrics = metrics;
    this.port = port;
    server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", this);
  }

  public void start() {
    System.out.println("[*] Server listening on port: " + port);
    this.server.start();
  }

  @Override
  public void handle(HttpExchange t) throws IOException {
     Headers resHeaders = t.getResponseHeaders();
    resHeaders.set("Content-type", "application/json");
    String body = metricsJson();
    long contentLength = body.getBytes(StandardCharsets.UTF_8).length;
    t.sendResponseHeaders(200, contentLength);

    OutputStream os = t.getResponseBody();
    os.write(body.getBytes());
    os.close();
  }

  private String metricsJson() {
    String json = "";
    ObjectMapper jsonMapper = new ObjectMapper()
      .registerModule((new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false)));
    try {
      json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metrics);
    } catch (JsonProcessingException e) {
      json = "Fail to parse json";
    }
    return json;
  }
}
