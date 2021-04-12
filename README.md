# Jenkins Demo Java Agent for GSoC 2021

## What is this?

This is a demo Java agent for Jenkins remoting.jar. This Java agent launches an HTTP server and exposes the agent and controller's connection status in JSON format.

#### **DO NOT** use in production.

## Launch

### Prepare for Plugin Development

see https://www.jenkins.io/doc/developer/tutorial/prepare/

### Start up

1. clone this repository.

2. execute the following command at the project root.

```
$ mvn package
// $(pwd)/target/agent-jar-with-dependencies.jar will be created
```

3. launch some Jenkins controller

4. connect agent with -javaagent option

```sh
java -javaagent:$(pwd)/target/agent-jar-with-dependencies.jar -jar agent.jar -jnlpUrl <jenkins path>/computer/<node-name>/jenkins-agent.jnlp -workDir "/opt/jenkins"
```

### Access to the agent's metrics

access http://<agent.host>:9000/

```json
// response
{
  "version": "4.0.0",
  "gauges": {
    "remote.connection.isConnected": {
      "value": true
    }
  },
  "counters": {},
  "histograms": {},
  "meters": {},
  "timers": {}
}
```

You can confirm agent-controller connection is up.

### Disconnect the agent

1. Disconnect the agent from Web UI.

2. Access http://<agent.host>:9000/

```json
// response
{
  "version": "4.0.0",
  "gauges": {
    "remote.connection.isConnected": {
      "value": false
    }
  },
  "counters": {},
  "histograms": {},
  "meters": {},
  "timers": {}
}
```

You can confirm agent-controller connection is down.

### Transformation of remoting.jar

In this Java agent, transform `hudson.remoting.EngineListenerSplitter` as below using javassist.

```java
// from
public class EngineListenerSplitter implements EngineListener {
  ...
  @Override
  public void status(String msg) {
    for (EngineListener l : listeners) {
      l.status(msg);
    }
  }
  ...
}
```

```java
// to
public class EngineListenerSplitter implements EngineListener {
  ...
  @Override
  public void status(String msg) {
    io.jenlins.plugins.demo.App.status(msg); // insert this line to get connection status
    for (EngineListener l : listeners) {
      l.status(msg);
    }
  }
  ...
}
```
