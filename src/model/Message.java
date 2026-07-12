package model;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class Message implements Serializable {
    private final String topic;
    private final int partition;
    private final long offset;
    private final long timestamp;
    private final byte[] key;
    private final byte[] value;
    private final String idempotencyKey;



    public Message(String topic, int partition, long offset, long timestamp,
                   byte[] key, byte[] value, String idempotencyKey) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
        this.idempotencyKey = idempotencyKey;
    }

    // Getters
    public String getTopic() { return topic; }
    public int getPartition() {
        return partition;
    }
    public long getOffset() {
        return offset;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public byte[] getKey() {
        return key;
    }
    public byte[] getValue() {
        return value;
    }
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * Serializes this message to a ByteBuffer ready for reading.
     * The returned buffer has already been flipped — callers must
     * read from it directly (get/getInt/getLong) and must NOT call
     * flip() again. Flipping twice collapses limit to position (0),
     * silently producing a zero-length write.
     */
    public ByteBuffer serialize() {
        byte[] topicBytes = topic.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] idempotencyKeyBytes = idempotencyKey != null ?
                idempotencyKey.getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0];

        int keyLen = key != null ? key.length : 0;
        int valueLen = value != null ? value.length : 0;

        // Calculate total size
        int size = 4 + topicBytes.length +
                4 + 4 + 8 + 8 +
                4 + keyLen +
                4 + valueLen +
                4 + idempotencyKeyBytes.length;

        ByteBuffer buffer = ByteBuffer.allocate(size);

        // Write topic
        buffer.putInt(topicBytes.length);
        buffer.put(topicBytes);

        // Write partition, offset, timestamp
        buffer.putInt(partition);
        buffer.putLong(offset);
        buffer.putLong(timestamp);

        // Write key
        buffer.putInt(keyLen);
        if (keyLen > 0) {
            buffer.put(key);
        }

        // Write value
        buffer.putInt(valueLen);
        if (valueLen > 0) {
            buffer.put(value);
        }

        // Write idempotency key
        buffer.putInt(idempotencyKeyBytes.length);
        if (idempotencyKeyBytes.length > 0) {
            buffer.put(idempotencyKeyBytes);
        }

        buffer.flip();
        return buffer;
    }

    public static Message deserialize(ByteBuffer buffer) {
        // Read topic
        int topicLen = buffer.getInt();
        byte[] topicBytes = new byte[topicLen];
        buffer.get(topicBytes);
        String topic = new String(topicBytes, java.nio.charset.StandardCharsets.UTF_8);

        // Read partition, offset, timestamp
        int partition = buffer.getInt();
        long offset = buffer.getLong();
        long timestamp = buffer.getLong();

        // Read key
        int keyLen = buffer.getInt();
        byte[] key = null;
        if (keyLen > 0) {
            key = new byte[keyLen];
            buffer.get(key);
        }

        // Read value
        int valueLen = buffer.getInt();
        byte[] value = null;
        if (valueLen > 0) {
            value = new byte[valueLen];
            buffer.get(value);
        }

        // Read idempotency key
        int idempotencyKeyLen = buffer.getInt();
        String idempotencyKey = null;
        if (idempotencyKeyLen > 0) {
            byte[] idempotencyKeyBytes = new byte[idempotencyKeyLen];
            buffer.get(idempotencyKeyBytes);
            idempotencyKey = new String(idempotencyKeyBytes, java.nio.charset.StandardCharsets.UTF_8);
        }

        return new Message(topic, partition, offset, timestamp, key, value, idempotencyKey);
    }

    @Override
    public String toString() {
        return String.format("Message{topic=%s, partition=%d, offset=%d, timestamp=%d, " +
                        "keyLen=%d, valueLen=%d, idempotencyKey=%s}",
                topic, partition, offset, timestamp,
                key != null ? key.length : 0,
                value != null ? value.length : 0,
                idempotencyKey);
    }
}