package com.example.llama.interfaces.shell;

import com.example.llama.domain.model.benchmark.BenchmarkResult;
import com.example.llama.domain.service.ModelOptimizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

import java.util.List;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class BenchmarkCommand {

    private final ModelOptimizer modelOptimizer;

    @ShellMethod(key = "benchmark", value = "Run performance benchmarks for LLM providers.")
    public void benchmark() {
        log.info("🚀 Starting LLM optimization benchmark suite...");
        
        List<BenchmarkResult> results = modelOptimizer.optimizeAll();
        
        if (results.isEmpty()) {
            log.warn("⚠️ No providers configured for benchmark.");
            return;
        }

        renderTable(results);
    }

    private void renderTable(List<BenchmarkResult> results) {
        String[][] data = new String[results.size() + 1][6];
        data[0] = new String[]{"Provider", "Model", "Format", "Compile", "Time(ms)", "TPS"};

        for (int i = 0; i < results.size(); i++) {
            BenchmarkResult r = results.get(i);
            data[i + 1] = new String[]{
                    r.getProvider(),
                    r.getModelName(),
                    r.isFormatSuccess() ? "✅" : "❌",
                    r.isCompileSuccess() ? "✅" : "❌",
                    String.valueOf(r.getTotalGenerationTimeMs()),
                    String.format("%.2f", r.getTps())
            };
        }

        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        tableBuilder.addFullBorder(BorderStyle.fancy_light);
        
        System.out.println("\n📊 LLM BENCHMARK REPORT");
        System.out.println(tableBuilder.build().render(100));
        System.out.println("🏁 Optimization suite complete.\n");
    }
}
