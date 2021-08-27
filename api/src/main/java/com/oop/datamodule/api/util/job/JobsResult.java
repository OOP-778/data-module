package com.oop.datamodule.api.util.job;

import java.util.Set;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JobsResult {
  private final Set<Throwable> errors;
  private final AtomicInteger completed;
}
