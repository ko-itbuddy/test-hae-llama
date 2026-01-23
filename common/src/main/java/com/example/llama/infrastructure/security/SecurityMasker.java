package com.example.llama.infrastructure.security;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 🛡️ SecurityMasker (LSP Implementation)
 * Enforces the Llama Security Protocol by sanitizing code before it leaves the
 * sandbox.
 */
@Component
public class SecurityMasker {

    private static final String TAG_BODY = "SEC:BODY";
    private static final String TAG_VAL = "SEC:VAL";
    private static final String TAG_DROP = "SEC:DROP";
    // SEC:MASK is handled best by replacing the statement with a comment,
    // but JavaParser comment association can be tricky.
    // For now, we focus on Structural (Body, Val, Drop).

    public String mask(String sourceCode) {
        if (sourceCode == null || sourceCode.isBlank())
            return sourceCode;

        CompilationUnit cu = StaticJavaParser.parse(sourceCode);

        // 1. Process SEC:DROP (Remove entire nodes)
        processDrops(cu);

        // 2. Process SEC:VAL (Mask values)
        processVals(cu);

        // 3. Process SEC:BODY (Hide implementation)
        processBodies(cu);

        // 4. Automated Heuristic Masking (Safety Net for missing tags)
        processHeuristics(cu);

        return cu.toString();
    }

    private void processHeuristics(CompilationUnit cu) {
        // Scan all string literals for sensitive patterns
        cu.findAll(StringLiteralExpr.class).forEach(str -> {
            String val = str.getValue();
            if (isSensitive(val)) {
                str.setValue("[AUTO_SECURED]");
            }
        });
    }

    private boolean isSensitive(String value) {
        if (value == null || value.length() < 8) return false;
        
        String lower = value.toLowerCase();

        // 1. URL with credentials (Improved pattern)
        if (value.matches(".*[a-zA-Z0-9]+://[^:]+:[^@]+@.*")) return true;
        
        // 2. Suspicious API Key patterns (e.g., sk-..., key-..., etc.)
        if (lower.contains("sk-") || 
            lower.contains("key-") ||
            lower.contains("token-") ||
            lower.contains("api_key") ||
            lower.contains("secret")) return true;
            
        // 3. High entropy strings (likely secrets/keys) - length > 20 and alphanumeric
        if (value.length() > 20 && value.matches("^[a-zA-Z0-9_\\-]+$")) {
            // Avoid marking package names or common paths as sensitive
            if (!value.contains(".") && !value.contains("/")) return true;
        }
        
        return false;
    }

    private void processDrops(CompilationUnit cu) {
        // Collect comments first to avoid concurrent modification issues if we were
        // iterating nodes directly
        List<Comment> dropComments = cu.getAllContainedComments().stream()
                .filter(c -> c.getContent().contains(TAG_DROP))
                .collect(Collectors.toList());

        for (Comment comment : dropComments) {
            comment.getCommentedNode().ifPresent(Node::remove);
            comment.remove(); // Remove the tag comment itself so usage is hidden
        }
    }

    private void processVals(CompilationUnit cu) {
        List<Comment> valComments = cu.getAllContainedComments().stream()
                .filter(c -> c.getContent().contains(TAG_VAL))
                .collect(Collectors.toList());

        for (Comment comment : valComments) {
            comment.getCommentedNode().ifPresent(node -> {
                if (node instanceof FieldDeclaration) {
                    ((FieldDeclaration) node).getVariables().forEach(this::maskVariable);
                } else if (node instanceof VariableDeclarator) {
                    maskVariable((VariableDeclarator) node);
                }
            });
            // We keep the comment or replace it?
            // The spec says: Result (LLM View) -> String key = "[SECURED_VALUE]";
            // We can remove the comment or change it to notify it was secured.
            // Let's remove the directive comment to be clean.
            comment.remove();
        }
    }

    private void maskVariable(VariableDeclarator var) {
        Type type = var.getType();
        Expression maskedValue;

        if (type.isPrimitiveType()) {
            PrimitiveType pt = type.asPrimitiveType();
            if (pt.getType() == PrimitiveType.Primitive.BOOLEAN) {
                maskedValue = new BooleanLiteralExpr(false);
            } else {
                maskedValue = new IntegerLiteralExpr("0");
            }
        } else if (type.asString().equals("String")) {
            maskedValue = new StringLiteralExpr("[SECURED_VALUE]");
        } else {
            maskedValue = new NullLiteralExpr();
        }

        var.setInitializer(maskedValue);
    }

    private void processBodies(CompilationUnit cu) {
        List<Comment> bodyComments = cu.getAllContainedComments().stream()
                .filter(c -> c.getContent().contains(TAG_BODY))
                .collect(Collectors.toList());

        for (Comment comment : bodyComments) {
            comment.getCommentedNode().ifPresent(node -> {
                if (node instanceof MethodDeclaration) {
                    maskMethodBody((MethodDeclaration) node);
                }
            });
            comment.remove();
        }
    }

    private void maskMethodBody(MethodDeclaration method) {
        BlockStmt newBody = new BlockStmt();

        // Add a comment inside explaining it's redacted
        newBody.addOrphanComment(new LineComment(" [SECURED: LOGIC REDACTED] "));

        if (!method.getType().isVoidType()) {
            newBody.addStatement(createDefaultReturn(method.getType()));
        }

        method.setBody(newBody);
    }

    private ReturnStmt createDefaultReturn(Type type) {
        Expression expr;
        if (type.isPrimitiveType()) {
            PrimitiveType pt = type.asPrimitiveType();
            switch (pt.getType()) {
                case BOOLEAN:
                    expr = new BooleanLiteralExpr(false);
                    break;
                case CHAR:
                    expr = new CharLiteralExpr(' ');
                    break;
                default:
                    expr = new IntegerLiteralExpr("0");
                    break;
            }
        } else if (type.asString().equals("String")) {
            expr = new StringLiteralExpr("[SECURED_VALUE]");
        } else if (type.asString().equals("Optional")) {
            MethodCallExpr emptyCall = new MethodCallExpr(
                    new NameExpr("java.util.Optional"), "empty");
            expr = emptyCall;
        } else {
            expr = new NullLiteralExpr();
        }
        return new ReturnStmt(expr);
    }
}
