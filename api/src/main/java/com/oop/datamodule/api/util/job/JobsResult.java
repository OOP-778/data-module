package com.oop.datamodule.api.util.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@Getter
public class JobsResult {
  private final List<Throwable> errors = new ArrayList<>();
  private final AtomicInteger completed = new AtomicInteger(0);
}
