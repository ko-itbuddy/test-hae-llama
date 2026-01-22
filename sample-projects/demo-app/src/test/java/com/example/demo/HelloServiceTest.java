package com.example.demo;

// Error: YOLO mode is enabled. All tool calls will be automatically approved.
YOLO mode is enabled. All tool calls will be automatically approved.
(node:17123) MaxListenersExceededWarning: Possible EventTarget memory leak detected. 11 abort listeners added to [AbortSignal]. MaxListeners is 10. Use events.setMaxListeners() to increase limit
(Use `node --trace-warnings ...` to show where the warning was created)
Error when talking to Gemini API Full report available at: /tmp/gemini-client-error-Turn.run-sendMessageStream-2026-01-22T10-35-01-952Z.json TerminalQuotaError: You have exhausted your capacity on this model. Your quota will reset after 11h54m54s.
    at classifyGoogleError (file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli-core/dist/src/utils/googleQuotaErrors.js:136:28)
    at retryWithBackoff (file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli-core/dist/src/utils/retry.js:130:37)
    at process.processTicksAndRejections (node:internal/process/task_queues:95:5)
    at async GeminiChat.makeApiCallAndProcessStream (file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli-core/dist/src/core/geminiChat.js:429:32)
    at async GeminiChat.streamWithRetries (file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli-core/dist/src/core/geminiChat.js:254:40)
    at async Turn.run (file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli-core/dist/src/core/turn.js:64:30)
    at async GeminiClient.processTurn (file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli-core/dist/src/core/client.js:457:26)
    at async GeminiClient.sendMessageStream (file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli-core/dist/src/core/client.js:553:20)
    at async file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli/dist/src/nonInteractiveCli.js:192:34
    at async main (file:///usr/local/share/.config/yarn/global/node_modules/@google/gemini-cli/dist/src/gemini.js:458:9) {
  cause: {
    code: 429,
    message: 'You have exhausted your capacity on this model. Your quota will reset after 11h54m54s.',
    details: [ [Object], [Object] ]
  },
  retryDelayMs: 42894102.052245
}
An unexpected critical error occurred:[object Object]