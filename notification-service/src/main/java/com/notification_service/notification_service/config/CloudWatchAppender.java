package com.notification_service.notification_service.config;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import org.springframework.context.annotation.Configuration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

@Configuration
public class CloudWatchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    
    private CloudWatchLogsClient client;
    
    private static final String LOG_GROUP_NAME_IN_CLOUDWATCH = "notification-service-logs";
    private static final String LOG_STREAM_NAME_IN_CLOUDWATCH = "console-logs";

    private Queue<InputLogEvent> eventQueue;

    public CloudWatchAppender() {
        client = CloudWatchLogsClient.builder().region(Region.SA_EAST_1).build();
        eventQueue = new LinkedList<>();
    }

    @Override
    protected void append(ILoggingEvent event) {
        // Construct the log message
        InputLogEvent logEvent = InputLogEvent.builder()
            .message(event.getLevel().levelStr + " " + event.getFormattedMessage())
            .timestamp(event.getTimeStamp())
            .build();

        // Add event to the queue
        eventQueue.add(logEvent);

        // Flush queue - Dev
        flushEvents();
    }

    private void flushEvents() {
        // Retrieve the existing log events
        DescribeLogStreamsResponse describeLogStreamsResponse = client.describeLogStreams(DescribeLogStreamsRequest.builder()
            .logGroupName(LOG_GROUP_NAME_IN_CLOUDWATCH)
            .logStreamNamePrefix(LOG_STREAM_NAME_IN_CLOUDWATCH)
            .build());

        if (describeLogStreamsResponse.logStreams().isEmpty()) {
            return; // nothing to flush
        }

        String sequenceToken = describeLogStreamsResponse.logStreams().get(0).uploadSequenceToken();

        // Batch up the next 10 events
        LinkedList<InputLogEvent> logEventsBatch = new LinkedList<>();
        while (!eventQueue.isEmpty() && logEventsBatch.size() < 10) {
            logEventsBatch.add(eventQueue.poll());
        }

        // Sort events before sending
        logEventsBatch.sort(Comparator.comparing(InputLogEvent::timestamp));

        // Check if logEventsBatch is empty
        if (logEventsBatch.isEmpty()) {
            return; // Skip the API call if there are no log events
        }

        // Put the log events into the CloudWatch stream
        PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
            .logGroupName(LOG_GROUP_NAME_IN_CLOUDWATCH)
            .logStreamName(LOG_STREAM_NAME_IN_CLOUDWATCH)
            .logEvents(logEventsBatch)
            .sequenceToken(sequenceToken)
            .build();

        client.putLogEvents(putLogEventsRequest);
    }

    @Override
    public void stop() {
        // Flush any remaining events before stopping
        flushEvents();

        // Clean up the AWS CloudWatchLogs client
        client.close();

        super.stop();
    }
}
