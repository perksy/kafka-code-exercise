#!/bin/bash
scriptdir=`dirname "$BASH_SOURCE"`
${scriptdir}/kafka_2.13-2.7.1/bin/kafka-streams-application-reset.sh --application-id kafka-code-exercise --force
