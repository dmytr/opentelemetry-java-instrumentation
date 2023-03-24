/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.zio.v2_0;

import io.opentelemetry.context.Context;
import javax.annotation.Nullable;

public final class FiberContext {
  private Context fiberContext;
  @Nullable private Context threadContext;

  private FiberContext(Context currentContext) {
    this.fiberContext = currentContext;
    this.threadContext = null;
  }

  public static FiberContext create() {
    return new FiberContext(Context.current());
  }

  public void onSuspend() {
    this.fiberContext = Context.current();

    // Reset context to avoid leaking it to other fibers
    if (this.threadContext != null) {
      this.threadContext.makeCurrent();
    }
  }

  public void onResume() {
    this.threadContext = Context.current();

    // Not using returned Scope because we can't reliably close it. If fiber also opens a Scope and
    // does not close it before onSuspend is called then the attempt to close the scope returned
    // here would not work because it is not the current scope.
    // See https://github.com/open-telemetry/opentelemetry-java/issues/5303
    this.fiberContext.makeCurrent();
  }
}
