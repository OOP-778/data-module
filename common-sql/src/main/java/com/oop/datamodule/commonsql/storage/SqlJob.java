package com.oop.datamodule.commonsql.storage;

import com.oop.datamodule.api.util.job.Job;

public class SqlJob extends Job {
  private final Runnable runnable;

  protected SqlJob(Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public String getName() {
    return "sql-job";
  }

  @Override
  public void run() {
    runnable.run();
  }
}
