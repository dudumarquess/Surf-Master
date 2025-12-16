package com.surfmaster.llm;

import java.util.List;

public interface LlmClient {

    LlmCompletionResult chat(List<LlmMessage> messages, LlmChatOptions options);
}
