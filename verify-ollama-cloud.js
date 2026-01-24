const { Ollama } = require('ollama');

// Load environment variables if needed
const baseUrl = process.env.LLAMA_OLLAMA_BASE_URL || 'https://api.ollama.cloud';
const model = process.env.LLAMA_OLLAMA_MODEL || 'deepseek-v3.2:cloud';

const ollama = new Ollama({ host: baseUrl });

async function runBenchmark() {
  console.log(`🚀 Starting npm client benchmark for ${model} at ${baseUrl}...`);
  
  const startTime = Date.now();
  let ttft = 0;
  let fullContent = '';
  let tokenCount = 0;

  try {
    const response = await ollama.chat({
      model: model,
      messages: [
        { role: 'system', content: 'You are a technical assistant. Respond concisely.' },
        { role: 'user', content: 'Generate a simple JUnit 5 test for a Java Calculator class.' }
      ],
      stream: true,
    });

    for await (const part of response) {
      if (ttft === 0) {
        ttft = Date.now() - startTime;
        console.log(`⏱️ TTFT: ${ttft}ms`);
      }
      fullContent += part.message.content;
      tokenCount++; 
    }

    const totalTime = Date.now() - startTime;
    const tps = (tokenCount / (totalTime / 1000)).toFixed(2);

    console.log('\n📊 BENCHMARK SUMMARY (npm client)');
    console.log('--------------------------------');
    console.log(`Total Time: ${totalTime}ms`);
    console.log(`TTFT:       ${ttft}ms`);
    console.log(`TPS:        ${tps}`);
    console.log(`Content Length: ${fullContent.length} chars`);
    console.log('--------------------------------\n');

  } catch (error) {
    console.error('❌ Benchmark failed:', error.message);
    process.exit(1);
  }
}

runBenchmark();