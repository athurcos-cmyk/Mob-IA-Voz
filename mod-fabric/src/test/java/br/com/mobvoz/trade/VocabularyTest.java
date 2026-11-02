package br.com.mobvoz.trade;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * O jogador nem sempre diz o nome certo do item: "uma armadura muito boa", "armadura ferro", "armadura de ferro boa", "bonimil", "a melhor espada".
 * Cada linha: o que ele fala -> o item que o servidor deve entender. Quando o pedido e vago, o servidor ESCOLHE um item concreto
 * (qualidade: simples = couro/madeira, boa = ferro, muito boa = diamante, a melhor = netherite; sem adjetivo = ferro).
 */
class VocabularyTest {
    private static final String[][] CASES = {
        // armadura: material, vago, qualidade, sem preposicao
        {"armadura de ferro", "iron_chestplate"}, {"armadura de ferro boa", "iron_chestplate"}, {"armadura ferro", "iron_chestplate"},
        {"uma armadura de ferro bem boa", "iron_chestplate"}, {"armadura de diamante", "diamond_chestplate"}, {"armadura diamante", "diamond_chestplate"},
        {"armadura de couro", "leather_chestplate"}, {"armadura de ouro", "golden_chestplate"}, {"armadura de netherite", "netherite_chestplate"},
        {"armadura de netherita", "netherite_chestplate"}, {"armadura", "iron_chestplate"}, {"uma armadura", "iron_chestplate"}, {"uma armadura boa", "iron_chestplate"},
        {"uma armadura muito boa", "diamond_chestplate"}, {"uma armadura bem forte", "diamond_chestplate"}, {"uma armadura excelente", "diamond_chestplate"},
        {"a melhor armadura", "netherite_chestplate"}, {"a armadura mais forte que você tiver", "netherite_chestplate"}, {"uma armadura simples", "leather_chestplate"},
        {"uma armadura qualquer", "leather_chestplate"}, {"armadura completa de ferro", "iron_chestplate"}, {"colete de ferro", "iron_chestplate"},
        {"peitoral de ferro", "iron_chestplate"}, {"couraça de diamante", "diamond_chestplate"},
        // peças
        {"capacete de ferro", "iron_helmet"}, {"elmo de ferro", "iron_helmet"}, {"um capacete bom", "iron_helmet"}, {"capacete muito bom", "diamond_helmet"},
        {"calça de ferro", "iron_leggings"}, {"calças de ferro", "iron_leggings"}, {"perneiras de diamante", "diamond_leggings"},
        {"botas de ferro", "iron_boots"}, {"bota de diamante", "diamond_boots"}, {"botas boas", "iron_boots"}, {"botas", "iron_boots"},
        // armas e ferramentas
        {"espada", "iron_sword"}, {"uma espada boa", "iron_sword"}, {"uma espada muito boa", "diamond_sword"}, {"a melhor espada", "netherite_sword"},
        {"espada de diamante", "diamond_sword"}, {"espada diamante", "diamond_sword"}, {"espada de ferro boa", "iron_sword"}, {"uma arma boa", "iron_sword"},
        {"picareta", "iron_pickaxe"}, {"picareta muito boa", "diamond_pickaxe"}, {"picareta de ouro", "golden_pickaxe"}, {"picareta diamante", "diamond_pickaxe"},
        {"pá de ferro", "iron_shovel"}, {"pa de diamante", "diamond_shovel"}, {"uma pá", "iron_shovel"}, {"machado", "iron_axe"}, {"machado de diamante", "diamond_axe"},
        {"enxada de ferro", "iron_hoe"}, {"uma ferramenta boa", "iron_pickaxe"}, {"arco", "bow"}, {"escudo", "shield"}, {"flechas", "arrow"},
        // materiais e blocos
        {"ferro", "iron_ingot"}, {"barra de ferro", "iron_ingot"}, {"lingote de ferro", "iron_ingot"}, {"ouro", "gold_ingot"}, {"diamante", "diamond"},
        {"diamantes", "diamond"}, {"esmeraldas", "emerald"}, {"carvão", "coal"}, {"madeira", "oak_log"}, {"tábuas", "oak_planks"}, {"lã", "white_wool"},
        {"couro", "leather"}, {"linha", "string"}, {"pólvora", "gunpowder"}, {"pérola do fim", "ender_pearl"}, {"pérola do ender", "ender_pearl"},
        {"olho de aranha", "spider_eye"}, {"osso", "bone"}, {"ossos", "bone"},
        // comida
        {"comida", "cooked_beef"}, {"uma comida boa", "cooked_beef"}, {"carne", "beef"}, {"carne assada", "cooked_beef"}, {"pão", "bread"}, {"maçã", "apple"},
        {"maçã dourada", "golden_apple"}, {"cenouras", "carrot"}, {"bolo", "cake"},
        // objetos e apelidos
        {"composteira", "composter"}, {"compostagem", "composter"}, {"bonimil", "bone_meal"}, {"farinha de osso", "bone_meal"}, {"bone meal", "bone_meal"},
        {"dinamite", "tnt"}, {"tnt", "tnt"}, {"baú", "chest"}, {"fornalha", "furnace"}, {"forno", "furnace"}, {"mesa de trabalho", "crafting_table"},
        {"bancada", "crafting_table"}, {"cama", "white_bed"}, {"tocha", "torch"}, {"balde", "bucket"}, {"balde de água", "water_bucket"}, {"livro", "book"},
        {"élitro", "elytra"}, {"asas", "elytra"}, {"totem", "totem_of_undying"}, {"tridente", "trident"},
        // erros de fala/escrita (o reconhecimento de voz erra)
        {"espada de dimante", "diamond_sword"}, {"composteria", "composter"}, {"capacette de ferro", "iron_helmet"}, {"picareta de diamanti", "diamond_pickaxe"},
    };

    /** Frases comuns que NAO sao pedido de item nao podem virar item por engano (a correcao de fala e a escolha por categoria so agem com palavra de item). */
    @Test
    void ordinaryWordsAreNotItems() {
        for (String w : new String[] {"oi tudo bem", "uma coisa legal", "alguma coisa", "meu amigo", "ajuda", "um favor", "uma historia", "segredo", "voce quer ser meu amigo",
                "quero conversar", "o que voce faz", "muito obrigado", "uma surpresa", "qualquer coisa", "algo bom", "presente"}) {
            assertTrue(Values.lookup(w).isEmpty(), "\"" + w + "\" nao e um item, mas virou " + Values.lookup(w));
        }
    }

    @Test
    void playerVocabularyResolvesToARealItem() {
        List<String> falhas = new ArrayList<>();
        for (String[] c : CASES) {
            Optional<String> got = Values.lookup(c[0]);
            String want = "minecraft:" + c[1];
            if (got.isEmpty() || !got.get().equals(want)) {
                falhas.add("\"" + c[0] + "\" -> " + got.orElse("(nada)") + "  (esperado " + c[1] + ")");
            }
        }
        System.out.println("VOCAB: " + (CASES.length - falhas.size()) + "/" + CASES.length + " entendidos");
        assertTrue(falhas.isEmpty(), "\n" + String.join("\n", falhas));
    }
}
