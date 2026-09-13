package com.eduflex.config;

import java.util.UUID;

public record ContentChangedEvent(ContentType type, UUID id) {
  public enum ContentType { COURSE, LESSON }
}
