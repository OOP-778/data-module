package com.oop.datamodule.api.util.job;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class JobsResult {
  private final List<Throwable> errors = new ArrayList<>();
  private final AtomicInteger completed = new AtomicInteger(0);
}
