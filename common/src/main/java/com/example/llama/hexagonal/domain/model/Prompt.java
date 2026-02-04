package com.example.llama.hexagonal.domain.model;

public record Prompt(String systemMessage, String userMessage) {
    
    public String toXml() {
        return String.format("""
                <prompt>
                    <system><![CDATA[%s]]></system>
                    <user><![CDATA[%s]]></user>
                </prompt>
                """, systemMessage, userMessage);
    }
    
    @Override
    public String toString() {
        return toXml();
    }
}
