package me.limhax.openAC.processor;

import lombok.Getter;
import me.limhax.openAC.data.PlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles lag compensation using Pledge's ping/transaction tracking system.
 * 
 * Pledge sends ping packets at the start and end of each server tick.
 * When the client responds to these pings, we know they've processed all
 * data sent before that ping (TCP guarantees ordered delivery).
 */
public class ConnectionProcessor {
  private final PlayerData data;

  // Queue of runnables waiting to be executed on the next pong
  private final ConcurrentLinkedQueue<Runnable> queuedTasks = new ConcurrentLinkedQueue<>();

  // Map of ping ID -> tasks to run when that ping is confirmed
  private final Map<Integer, List<Runnable>> pendingTasks = new ConcurrentHashMap<>();

  @Getter
  private volatile int currentTickPingId = Integer.MIN_VALUE;

  public ConnectionProcessor(PlayerData data) {
    this.data = data;
  }

  /**
   * Queues a runnable to be executed when the next tick's ping packet is confirmed by the client.
   * This provides lag compensation by ensuring the client has processed all data sent before the ping.
   *
   * @param runnable The task to execute upon confirmation
   */
  public void runOnPong(Runnable runnable) {
    queuedTasks.add(runnable);
  }

  /**
   * Called by Pledge listener when the end-of-tick ping is sent.
   * Associates all queued tasks with this ping ID.
   *
   * @param pingId The ID of the ping packet sent at end of tick
   */
  public void onPingSendEnd(int pingId) {
    List<Runnable> tasksForThisPing = new ArrayList<>();
    Runnable task;
    while ((task = queuedTasks.poll()) != null) {
      tasksForThisPing.add(task);
    }

    if (!tasksForThisPing.isEmpty()) {
      pendingTasks.put(pingId, tasksForThisPing);
    }
    this.currentTickPingId = pingId;
  }

  /**
   * Called by Pledge listener when a pong is received for the end-of-tick ping.
   * Executes all tasks associated with that ping ID.
   *
   * @param pingId The ID of the pong received
   */
  public void onPongReceiveEnd(int pingId) {
    List<Runnable> tasks = pendingTasks.remove(pingId);
    if (tasks != null) {
      for (Runnable runnable : tasks) {
        try {
          runnable.run();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }


  public void cleanup() {
    pendingTasks.clear();
    queuedTasks.clear();
  }
}
