package br.com.mobvoz.client;

import java.util.List;

/** Texto do {@code /mobvoz ajuda}: como falar com as criaturas, ganhar confianca, chamar pelo nome e todos os comandos. */
public final class HelpText {
    /** Todos os subcomandos de {@code /mobvoz} que o texto precisa explicar (um teste confere). */
    public static final List<String> COMMANDS = List.of("voz", "chat", "legenda", "falar", "memoria", "conversas", "dificuldade", "status", "ajuda");

    private HelpText() {
    }

    public static List<String> lines() {
        return List.of(
                "§6=== MobVoz IA: ajuda ===§r",
                "§eFalar:§r mire numa criatura (aparece uma estrela) e fale com o microfone; sem microfone, use /mobvoz chat sim e escreva no chat.",
                "§eConfiança:§r converse e dê presentes (SHIFT + botão direito com o item na mão). A barra na tela mostra quanto falta para ela aceitar te seguir.",
                "§eApelido:§r diga \"vou te chamar de Mimosa\". Depois chame pelo nome, sem mirar: \"Mimosa, vem aqui!\" (até 32 blocos).",
                "§eComandos:§r",
                " /mobvoz voz sim|nao: liga ou desliga o microfone",
                " /mobvoz chat sim|nao: modo chat (digitar para a criatura da mira)",
                " /mobvoz legenda sim|nao: mostra o que as criaturas dizem",
                " /mobvoz falar <texto>: fala com a criatura da mira",
                " /mobvoz memoria: o que a criatura da mira sabe de você",
                " /mobvoz conversas [nenhuma|poucas|normal|muitas]: quanto as criaturas conversam entre si (padrão: poucas)",
                " /mobvoz dificuldade [facil|normal|dificil]: quão difícil é ganhar confiança",
                " /mobvoz status: estado do serviço de IA",
                " /mobvoz ajuda: esta tela",
                "§7Se uma criatura só disser que não entendeu, olhe a janela preta do serviço de IA (3-LIGAR-SERVICO.bat): ela diz o que fazer.§r");
    }
}
