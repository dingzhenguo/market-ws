package com.market.common.messages.bridge;

import com.market.common.def.MessageType;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yjt
 * @since 2020/10/13 上午9:00
 */
@Data
public class PriceChangeMessage {
    /**
     * 交易对
     */
    private String symbol;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 是否为第三方数据
     * <p>
     * 该字段已经废用
     */
    @Deprecated
    private Boolean third;

    /**
     * 采用该字段表示数据来源
     */
    private String source;

    public PriceChangeMessage() {
    }

    public PriceChangeMessage(TradeMessage msg, String source) {
        this.symbol = msg.getSymbol();
        this.price = msg.getPrice();
        this.source = source;
    }

    public static Message<PriceChangeMessage> ofLocal(String symbol,
                                                      BigDecimal price) {
        return of(symbol, price, false);
    }

    public static Message<PriceChangeMessage> ofThird(String symbol,
                                                      BigDecimal price) {
        return of(symbol, price, true);
    }

    private static Message<PriceChangeMessage> of(String symbol,
                                                  BigDecimal price,
                                                  boolean third) {
        PriceChangeMessage data = new PriceChangeMessage();
        data.setSymbol(symbol);
        data.setPrice(price);
        data.setThird(third);
        Message<PriceChangeMessage> msg = new Message<>();
        msg.setType(MessageType.MARKET_PRICE);
        msg.setData(data);
        return msg;
    }

    public static Buffer toBuf(PriceChangeMessage pc) {
        // | msg size (4byte) | msg type (1byte) | ts (8byte) |
        // | symbol.size [4byte] data[bytes] | price (8byte) | third (1byte)
        byte[] symbolBytes = pc.getSymbol().getBytes();
        int msgSize = 22 + symbolBytes.length;
        return Buffer.buffer(msgSize)
                .appendInt(msgSize)
                .appendByte((byte) MessageType.MARKET_PRICE.ordinal())
                .appendLong(System.currentTimeMillis())
                .appendInt(symbolBytes.length)
                .appendBytes(symbolBytes)
                .appendDouble(pc.getPrice().doubleValue())
                .appendByte((byte) (Boolean.TRUE.equals(pc.getThird()) ? 1 : 0));
    }

    public static PriceChangeMessage of(Buffer buf, int readOffset, int msgSize) {
        // | msg size (4byte) | msg type (1byte) | ts (8byte) |
        // | symbol.size [4byte] data[bytes] | price (8byte) | third (1byte)
        int offset = readOffset;
        PriceChangeMessage pc = new PriceChangeMessage();
        int symbolLength = buf.getInt(offset);
        if (symbolLength != msgSize - 22) {
            return null;
        }
        offset += 4;
        byte[] symbolBytes = buf.getBytes(offset, offset + symbolLength);
        offset += symbolLength;
        pc.setSymbol(new String(symbolBytes));
        pc.setPrice(BigDecimal.valueOf(buf.getDouble(offset)));
        offset += 8;
        byte third = buf.getByte(offset);
        pc.setThird(third == 1);
        return pc;
    }
}
