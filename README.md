# Perksy Backend Coding Exercise

## Setup

* Unzip Kafka distribution. It's being used for the `kafka-streams-application-reset.sh` utility
called by the `reset.sh` script
* Run `docker compose up` to start up a single Kafka node. (Run as a daemon if you choose to)
* Check to make sure kafka is running with `docker compose ps`. Run `docker compose start kafka` if
necessary.
* Import the gradle project in this directory into the IDE of your choice

## Validate Setup

Run the gradle `bootRun` task to make sure the app starts up

## Goals

There are a few potential goals to attempt to complete as much as for this exercise. The intention
of the exercise is not necessarily to demonstrate your existing knowledge of the components you'll
be working with (Kafka Streams, primarily) but more to provide a demonstration of your problem-
solving process. It is expected that you will spend much of your time running into problems that
you will need to search for solutions for in any way that you're able to quickly, for example,
searching online, reading documentation, exploring APIs in your IDE, etc.

When the application starts up, it will automatically create two input tables and seed them with
data. This logic is in the `SampleDataProducer` class and should not need to be modified.

The data model is in the `model` package and contains two entities, `Order` and `OrderItem`. The
input tables will contain a number of items for each entity type. Names of the input tables are
declared as constants in the `KafkaStreamsConfig` class.

The standard Java Kafka-Streams DSL/API has been augmented with extensions and utility methods
in order to simplify the usage of serializers/deserialization, as well as streamline boilderplate
code. If you wish to use or see the implementation of these, please look inside `Materializations.kt`,
`Processors.kt` and `Serdes.kt`. These extensions, when applied, have been configured to handle
basic value types into basic serialized form, as well as complex objects into JSON.

Your solution code should be in the form of a Kafka Streams topology declaration inside of the
`Solution` class

### First goal

Stream all of the `OrderItem`s and group/aggregate them according to their `orderId` property.
The results should be in the form of the `OrderResult` entity and should contain a correctly
calculated `totalAmount`. The result should be printed to output. Duplicate `OrderResult` items
of the same orderId are allowed as long as the last one contains the correct total. The `order`
property should remain null for this first goal.

### Second goal

Join the `Order` stream into the `OrderItem` stream so that the `OrderResult`s include the `order`
property.

### Third goal

Create a rest endpoint that queries the local datastore containing the `OrderResult` items
by `orderId` and returns the result.