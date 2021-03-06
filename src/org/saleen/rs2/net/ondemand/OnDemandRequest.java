package org.saleen.rs2.net.ondemand;

import java.io.IOException;

import org.jboss.netty.channel.Channel;
import org.saleen.cache.Cache;
import org.saleen.rs2.net.PacketBuilder;

/**
 * <p>
 * Represents a single 'ondemand' request. Ondemand requests are created when
 * the client requests a file from the cache using the update protocol.<?p>
 * 
 * @author Graham Edgecombe
 * 
 */
public class OnDemandRequest {

	/**
	 * The session.
	 */
	private Channel channel;

	/**
	 * The cache.
	 */
	private int cacheId;

	/**
	 * The file.
	 */
	private int fileId;

	/**
	 * The priority.
	 */
	private int priority;

	/**
	 * Creates the request.
	 * 
	 * @param channel
	 *            The session.
	 * @param cacheId
	 *            The cache.
	 * @param fileId
	 *            The file.
	 * @param priority
	 *            The priority.
	 */
	public OnDemandRequest(Channel channel, int cacheId, int fileId,
			int priority) {
		this.channel = channel;
		this.cacheId = cacheId;
		this.fileId = fileId;
		this.priority = priority;
	}

	/**
	 * Services the request.
	 */
	public void service(Cache cache) {
		try {
			byte[] data = cache.getFile(cacheId + 1, fileId).getBytes();
			int totalSize = data.length;
			int roundedSize = totalSize;
			while (roundedSize % 500 != 0)
				roundedSize++;
			int blocks = roundedSize / 500;
			int sentBytes = 0;
			for (int i = 0; i < blocks; i++) {
				int blockSize = totalSize - sentBytes;
				PacketBuilder bldr = new PacketBuilder();
				bldr.put((byte) cacheId);
				bldr.put((byte) (fileId >> 8));
				bldr.put((byte) fileId);
				bldr.put((byte) (totalSize >> 8));
				bldr.put((byte) totalSize);
				bldr.put((byte) i);
				if (blockSize > 500) {
					blockSize = 500;
				}
				bldr.put(data, sentBytes, blockSize);
				sentBytes += blockSize;
				channel.write(bldr.toPacket());
			}
		} catch (IOException ex) {
			channel.close();
			ex.printStackTrace();
		}
	}

	/**
	 * Gets the priority.
	 * 
	 * @return The priority.
	 */
	public int getPriority() {
		return priority;
	}

}
