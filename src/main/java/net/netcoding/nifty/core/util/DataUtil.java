package net.netcoding.nifty.core.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class DataUtil {

	public static String compress(String data) throws IOException {
		return compress(data.getBytes());
	}

	public static String compress(String data, int level) throws IOException {
		return compress(data.getBytes(), level);
	}

	public static String compress(byte[] data) throws IOException {
		return compress(data, 1);
	}

	public static String compress(byte[] data, int level) throws IOException {
		if (data.length == 0)
			return "";

		byte[] results;

		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			try (DeflaterOutputStream deflater = new DeflaterOutputStream(buffer, new Deflater(level))) {
				deflater.write(data);
			}

			results = buffer.toByteArray();
		}

		return Base64Coder.encodeLines(results);
	}

	public static byte[] decompress(String data) throws IOException {
		if (StringUtil.isEmpty(data))
			return new byte[] {};

		byte[] bytes = Base64Coder.decodeLines(data);
		byte[] results;

		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			try (InflaterOutputStream inflater = new InflaterOutputStream(buffer)) {
				inflater.write(bytes);
			}

			results = buffer.toByteArray();
		}

		return results;
	}

	public static ByteArrayDataInput newDataInput(byte[] data) {
		return ByteStreams.newDataInput(data);
	}

	public static ByteArrayDataInput newDataInput(ByteArrayInputStream inputStream) {
		return ByteStreams.newDataInput(inputStream);
	}

	public static ByteArrayDataInput newDataInput(byte[] data, int start) {
		return ByteStreams.newDataInput(data, start);
	}

	public static ByteArrayDataOutput newDataOutput() {
		return ByteStreams.newDataOutput();
	}

	public static ByteArrayDataOutput newDataOutput(ByteArrayOutputStream outputStream) {
		return ByteStreams.newDataOutput(outputStream);
	}

	public static ByteArrayDataOutput newDataOutput(int size) {
		return ByteStreams.newDataOutput(size);
	}

	public static int readVarInt(DataInputStream in) throws IOException {
		int i = 0;
		int j = 0;

		while (true) {
			int k = in.readByte();
			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) throw new RuntimeException("VarInt too big");
			if ((k & 0x80) != 128) break;
		}

		return i;
	}

	public static int readVarInt(ByteArrayDataInput in) {
		int i = 0;
		int j = 0;

		while (true) {
			int k = in.readByte();
			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) throw new RuntimeException("VarInt too big");
			if ((k & 0x80) != 128) break;
		}

		return i;
	}

    public static void writeByteArray(DataOutputStream out, byte[] data) throws IOException {
    	writeVarInt(out, data.length);
        out.write(data);
    }

	public static void writeByteArray(ByteArrayDataOutput out, byte[] data) throws IOException {
    	writeVarInt(out, data.length);
        out.write(data);
	}

    public static void writeString(DataOutputStream out, String string) throws IOException {
        writeVarInt(out, string.length());
        out.write(string.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeString(ByteArrayDataOutput out, String string) throws IOException {
        writeVarInt(out, string.length());
        out.write(string.getBytes(StandardCharsets.UTF_8));
    }

	public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.writeByte(paramInt);
				return;
			}

			out.writeByte(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}

	public static void writeVarInt(ByteArrayDataOutput out, int paramInt) {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.writeByte(paramInt);
				return;
			}

			out.writeByte(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}

}