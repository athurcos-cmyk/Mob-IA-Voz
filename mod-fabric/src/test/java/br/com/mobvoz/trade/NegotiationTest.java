package br.com.mobvoz.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.trade.Negotiation.Stack;
import br.com.mobvoz.trade.Negotiation.Verdict;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NegotiationTest {
    private static final Map<String, Integer> NONE = Map.of();
    private static final String HAY = "minecraft:hay_block";
    private static final String IRON = "minecraft:iron_ingot";
    private static final String GOLD = "minecraft:gold_ingot";
    private static final String PICK = "minecraft:diamond_pickaxe";

    /** O exemplo do jogador: o aldeao pede 3 fenos; ele joga 4 -> toma 3, devolve 1. */
    @Test
    void farmerWantsThreeHayAndTheFourthIsReturned() {
        var farmer = Profiles.of("minecraft:villager#farmer");
        double price = TradeCatalog.forProfile("minecraft:villager#farmer").orElseThrow().price();
        var sel = Negotiation.select(farmer, NONE, List.of(new Stack(7, HAY, 4, 1.0)), price);
        assertTrue(sel.enough());
        assertEquals(List.of(new Negotiation.Take(7, HAY, 3)), sel.takes());
        assertEquals(List.of(new Negotiation.Take(7, HAY, 1)), sel.leftovers(), "o 4o feno volta");
    }

    @Test
    void notEnoughMeansNothingIsTaken() {
        var farmer = Profiles.of("minecraft:villager#farmer");
        var sel = Negotiation.select(farmer, NONE, List.of(new Stack(1, HAY, 2, 1.0)), 13.0);
        assertFalse(sel.enough());
        assertTrue(sel.takes().isEmpty(), "sem valor suficiente o negocio nao fecha e nada e tomado");
    }

    /** Pagamento misto: o que o oficio aceita soma valor (trigo + feno). */
    @Test
    void mixedPaymentsAddUp() {
        var farmer = Profiles.of("minecraft:villager#farmer");
        var sel = Negotiation.select(farmer, NONE, List.of(new Stack(1, HAY, 2, 1.0), new Stack(2, "minecraft:wheat", 10, 2.0)), 13.0);
        assertTrue(sel.enough());
        assertEquals(2, sel.takes().get(0).count());
        assertEquals(8, sel.takes().get(1).count(), "8 trigos (4) completam os 9 do feno ate 13");
        assertEquals(2, sel.leftovers().get(0).count());
    }

    /** Itens fora do oficio nao entram na conta: o ferreiro nao aceita feno. */
    @Test
    void itemsOutsideTheTradeAreIgnored() {
        var smith = Profiles.of("minecraft:villager#toolsmith");
        assertEquals(0.0, Negotiation.interest(smith, HAY, NONE));
        var sel = Negotiation.select(smith, NONE, List.of(new Stack(1, HAY, 64, 1.0)), 10.0);
        assertFalse(sel.enough());
    }

    /** Esqueleto Wither: ferro nao interessa, a picareta de diamante sim (e vale quase tudo). */
    @Test
    void witherSkeletonIgnoresIronButLikesTheDiamondPickaxe() {
        var ws = Profiles.of("minecraft:wither_skeleton");
        assertEquals(0.0, Negotiation.interest(ws, IRON, NONE));
        assertEquals(1.0, Negotiation.interest(ws, PICK, NONE));
        Map<String, Integer> offered = Map.of(IRON, 3, PICK, 1);
        assertEquals(40.0, Negotiation.acceptedValue(ws, NONE, offered), 1e-9, "so a picareta conta");
        double price = TradeCatalog.byId("wither_skeleton_wither_skeleton_skull_v2").orElseThrow().price();
        assertTrue(Negotiation.acceptedValue(ws, NONE, offered) < price, "ainda nao basta: precisa de mais valor");
        assertTrue(Negotiation.acceptedValue(ws, NONE, Map.of(PICK, 1, GOLD, 4)) >= price);
    }

    /** Convencer: argumento forte muda o interesse, mas com limites e sem forcar aversao. */
    @Test
    void persuasionIsBoundedAndNeverBeatsAversion() {
        var ws = Profiles.of("minecraft:wither_skeleton");
        var weak = Negotiation.persuade(ws, IRON, 1, 0, 1, 0, 0);
        assertEquals(Verdict.PARTIAL, weak.verdict());
        var strong = Negotiation.persuade(ws, IRON, 1, 0, 2, 0, 0);
        assertEquals(Verdict.CONVINCED, strong.verdict());
        assertTrue(strong.newMilli() <= 800, "teto de interesse persuadido");
        assertEquals(Verdict.NO_ARGUMENT, Negotiation.persuade(ws, IRON, 2, 1.0, 0, 0, 0).verdict(), "sem motivo nao convence nem o confiante");
        assertEquals(Verdict.AVERSION, Negotiation.persuade(ws, "minecraft:water_bucket", 2, 1.0, 2, 0, 0).verdict());
        assertEquals(Verdict.FATIGUE, Negotiation.persuade(ws, IRON, 2, 1.0, 2, 500, Negotiation.MAX_PERSUASIONS).verdict());
        assertEquals(Verdict.ALREADY_INTERESTED, Negotiation.persuade(ws, PICK, 1, 0, 2, 0, 0).verdict());
        // quem acredita mais e confia mais e convencido com mais folga
        assertTrue(Negotiation.persuade(ws, IRON, 2, 0.8, 1, 0, 0).newMilli() > Negotiation.persuade(ws, IRON, 0, 0, 1, 0, 0).newMilli());
        // item convencido passa a valer como pagamento (em parte)
        Map<String, Integer> persuaded = Map.of(IRON, strong.newMilli());
        assertTrue(Negotiation.interest(ws, IRON, persuaded) > 0.4);
        assertTrue(Negotiation.acceptedValue(ws, persuaded, Map.of(IRON, 3)) > 0);
    }

    /** Coerencia do catalogo: o que a criatura da faz sentido para ela, o que aceita tem valor, nada e aceito E dado ao mesmo tempo. */
    @Test
    void catalogOnlyGivesWhatMakesSenseForTheCreature() {
        for (var r : TradeCatalog.ALL) {
            var p = Profiles.of(r.profile());
            assertEquals(r.profile(), p.id(), "toda receita tem perfil proprio: " + r.id());
            assertTrue(p.drops().contains(r.outputItem()), r.id() + ": " + r.outputItem() + " nao esta nos drops de " + p.id());
            assertFalse(p.wants().isEmpty(), r.id() + " sem itens aceitos");
            assertFalse(p.wants().contains(r.outputItem()), r.id() + ": aceita e da o mesmo item");
            for (String w : p.wants()) {
                assertTrue(Values.known(w), "item aceito sem valor: " + w);
            }
            assertTrue(Values.known(r.outputItem()), "saida sem valor: " + r.outputItem());
        }
        // drops de um perfil com receitas = exatamente as saidas das receitas (uma fonte so)
        for (var p : TradeCatalog.ALL.stream().map(TradeCatalog.Recipe::profile).distinct().toList()) {
            var outs = TradeCatalog.recipesFor(p).stream().map(TradeCatalog.Recipe::outputItem).collect(java.util.stream.Collectors.toSet());
            assertEquals(outs, Profiles.of(p).drops(), p);
        }
        assertTrue(TradeCatalog.forProfile("minecraft:cow").isPresent());
        assertTrue(TradeCatalog.forProfile("minecraft:creeper").isPresent(), "todo mob tem algo a oferecer: o creeper vende polvora");
        assertTrue(TradeCatalog.forProfile("othermod:blob").isEmpty(), "so um mob de outro mod, sem perfil, fica sem nada para vender");
        assertEquals(TradeCatalog.recipesFor("minecraft:villager"), TradeCatalog.recipesFor("minecraft:villager#nitwit"), "profissao sem receita propria usa a do aldeao");
    }

    /** O que a criatura da combina com a natureza dela (pedidos do jogador no wiki: peixe so do mar, camelo cacto, vaca couro...). */
    @Test
    void whatEachCreatureGivesMatchesItsNature() {
        java.util.function.Function<String, java.util.Set<String>> gives = p -> TradeCatalog.recipesFor(p).stream()
                .map(r -> r.outputItem().substring(10)).collect(java.util.stream.Collectors.toSet());
        assertTrue(java.util.Set.of("prismarine_shard", "sea_pickle", "prismarine_crystals").containsAll(gives.apply("minecraft:cod")), "peixe so da coisas do mar");
        assertTrue(gives.apply("minecraft:camel").contains("cactus"), "o camelo da cacto");
        assertFalse(gives.apply("minecraft:cod").contains("cactus"), "o peixe nunca da cacto");
        assertEquals(java.util.Set.of("egg", "feather"), gives.apply("minecraft:chicken"), "galinha: ovo e pena, nunca ferramenta");
        assertTrue(gives.apply("minecraft:cow").containsAll(java.util.Set.of("leather", "leather_boots", "leather_chestplate")), "vaca: couro");
        assertTrue(gives.apply("minecraft:iron_golem").containsAll(java.util.Set.of("iron_ingot", "iron_block", "iron_chestplate", "cobblestone")),
                "golem: ferro, bloco, armadura e pedra");
        // quem pasta da flores, e flores diferentes
        for (String grazer : List.of("minecraft:sheep", "minecraft:cow", "minecraft:horse")) {
            assertTrue(TradeCatalog.recipesFor(grazer).stream().anyMatch(r -> Values.isFlower(r.outputItem())), grazer + " sem flor");
        }
        assertNotEquals(TradeCatalog.forItem("minecraft:sheep", "minecraft:azure_bluet").orElseThrow().outputItem(),
                TradeCatalog.forItem("minecraft:horse", "minecraft:oxeye_daisy").orElseThrow().outputItem());
        // nenhuma criatura entrega ferramenta/armadura de diamante, e o que nao combina e "sem receita"
        assertTrue(TradeCatalog.forItem("minecraft:chicken", "minecraft:diamond_pickaxe").isEmpty());
        assertTrue(TradeCatalog.forItem("minecraft:chicken", "minecraft:iron_pickaxe").isEmpty());
        assertTrue(Values.isGenericFlower("uma flor") && Values.isGenericFlower("flores") && !Values.isGenericFlower("flor azul"));
    }

    /** Ninguem cria valor do nada: o que o jogador entrega vale pelo menos o que recebe (sem farm de "flores por ferro"). */
    @Test
    void noRecipeCreatesValueOutOfThinAir() {
        for (var r : TradeCatalog.ALL) {
            double receives = r.outputCount() * Values.value(r.outputItem());
            assertTrue(r.price() + 1e-9 >= receives, r.id() + ": paga " + r.price() + " e recebe " + receives);
        }
    }

    /** O mecanismo e GENERICO: todo mob com receita (golem, enderman, aranha...) negocia pelo mesmo motor. */
    @Test
    void theSameEngineServesEveryCreatureInTheCatalog() {
        for (String mob : List.of("minecraft:iron_golem", "minecraft:enderman", "minecraft:spider", "minecraft:skeleton", "minecraft:blaze",
                "minecraft:pig", "minecraft:villager#farmer", "minecraft:cod")) {
            var r = TradeCatalog.forProfile(mob).orElseThrow(() -> new AssertionError("sem receita: " + mob));
            var p = Profiles.of(mob);
            assertFalse(r.paymentExamples(1).isEmpty(), mob + " sem exemplos de pagamento");
            // pagando o exemplo, o servidor fecha o negocio com a quantidade minima
            String item = p.wants().stream().filter(i -> Values.value(i) > 0).max((a, b) -> Double.compare(Values.value(a), Values.value(b))).orElseThrow();
            int n = (int) Math.ceil(r.price() / Values.value(item) - 1e-9);
            var sel = Negotiation.select(p, Map.of(), List.of(new Stack(1, item, n + 5, 1.0)), r.price());
            assertTrue(sel.enough() && sel.takes().get(0).count() == n && sel.leftovers().get(0).count() == 5, mob);
        }
        assertEquals(List.of("minecraft:ender_pearl"), List.copyOf(Profiles.of("minecraft:enderman").drops()));
        assertTrue(Profiles.of("minecraft:enderman").wants().contains("minecraft:grass_block"), "enderman troca por blocos");
        assertTrue(Profiles.of("minecraft:spider").drops().contains("minecraft:cobweb"));
        assertTrue(Profiles.of("minecraft:iron_golem").drops().contains("minecraft:iron_ingot"));
    }

    @Test
    void everyVillagerProfessionTradesItsOwnGoods() {
        for (String prof : List.of("farmer", "toolsmith", "armorer", "weaponsmith", "librarian", "cleric", "fisherman", "shepherd",
                "butcher", "fletcher", "leatherworker", "cartographer", "mason")) {
            assertTrue(TradeCatalog.forProfile("minecraft:villager#" + prof).isPresent(), "sem receita para " + prof);
        }
        // o que cada oficio PEDE tem a ver com ele
        assertTrue(Profiles.of("minecraft:villager#librarian").wants().contains("minecraft:paper"));
        assertTrue(Profiles.of("minecraft:villager#fletcher").wants().contains("minecraft:feather"));
        assertTrue(Profiles.of("minecraft:villager#butcher").wants().contains("minecraft:beef"));
        assertFalse(Profiles.of("minecraft:villager#farmer").wants().contains("minecraft:iron_ingot"));
    }

    /** Item fora do catalogo (ex.: osso do esqueleto, picareta de ferro do golem): receita improvisada, sempre >= valor, a prova de adulteracao. */
    @Test
    void improvisedRecipesNeverCreateValue() {
        var r = TradeCatalog.dynamic("minecraft:iron_golem", "minecraft:iron_pickaxe", 1.5, 3).orElseThrow();
        assertEquals("minecraft:iron_pickaxe", r.outputItem());
        assertTrue(r.price() >= Values.value("minecraft:iron_pickaxe"), "preco " + r.price());
        assertEquals(r, TradeCatalog.byId(r.id()).orElseThrow(), "o id carrega a receita inteira (sobrevive a salvar/sair)");
        assertTrue(TradeCatalog.dynamic("minecraft:chicken", "minecraft:diamond_pickaxe", 2.5, 1).isEmpty(), "nada caro (>30) e improvisado");
        assertTrue(TradeCatalog.dynamic("minecraft:chicken", "minecraft:iron_pickaxe", 0.5, 1).isEmpty(), "margem < 1 criaria valor");
        assertTrue(TradeCatalog.byId("dyn|minecraft:chicken|minecraft:iron_pickaxe|1|1").isEmpty(), "id adulterado com preco baixo e recusado");
        assertTrue(TradeCatalog.byId("dyn|minecraft:chicken|minecraft:nao_existe|50|1").isEmpty());
        assertTrue(TradeCatalog.byId("dyn|lixo").isEmpty());
        assertFalse(r.conditions(1).isEmpty());
        // criatura sem gosto fixo: o preco sai em esmeraldas, nunca vazio
        var odd = TradeCatalog.dynamic("othermod:blob", "minecraft:gunpowder", 1.5, 3).orElseThrow();
        assertTrue(odd.paymentExamples(1).get(0).contains("esmeralda"), odd.paymentExamples(1).toString());
    }

    /** Sorte: determinista por (criatura, item, dia); em media ~15% (NO) e ~35% (MAYBE); outra criatura/dia pode dar outro resultado. */
    @Test
    void luckIsDeterministicAndRoughlyTheConfiguredChance() {
        var id = java.util.UUID.randomUUID();
        assertEquals(Trades.luck(id, "minecraft:iron_pickaxe", 3, "NO"), Trades.luck(id, "minecraft:iron_pickaxe", 3, "NO"));
        for (String[] c : new String[][] {{"NO", "0.15"}, {"MAYBE", "0.35"}}) {
            int hits = 0;
            for (int i = 0; i < 4000; i++) {
                hits += Trades.luck(java.util.UUID.randomUUID(), "minecraft:iron_pickaxe", 5, c[0]) ? 1 : 0;
            }
            assertEquals(Double.parseDouble(c[1]), hits / 4000.0, 0.04, c[0]);
        }
    }

    /** Julgamento da IA vira interesse parcial (nao conta como tentativa de convencer) e nunca rebaixa o que ja existe. */
    @Test
    void judgedInterestIsPartialAndDoesNotCountAsPersuasion() {
        var t = MobTrades.EMPTY.open("x", "p", 100).withJudged("minecraft:raw_iron", 700);
        assertEquals(0, t.persuasions());
        assertEquals(0.7, Negotiation.interest(Profiles.of("minecraft:iron_golem"), "minecraft:raw_iron", t.persuadedMap()), 1e-9);
        assertEquals(700, t.withJudged("minecraft:raw_iron", 350).persuadedMap().get("minecraft:raw_iron"));
        // o valor aceito e proporcional: 3 ferros brutos (2.0 cada) a 70%
        assertEquals(4.2, Negotiation.acceptedValue(Profiles.of("minecraft:iron_golem"), t.persuadedMap(), Map.of("minecraft:raw_iron", 3)), 1e-9);
    }

    @Test
    void spokenNamesResolveToRealItems() {
        assertEquals("minecraft:iron_ingot", Values.lookup("3 ferros").orElseThrow() .equals("minecraft:iron_ingot") ? "minecraft:iron_ingot" : "");
        assertEquals("minecraft:diamond_pickaxe", Values.lookup("a picareta de diamante").orElseThrow());
        assertEquals("minecraft:wither_skeleton_skull", Values.lookup("cabeças de esqueleto Wither").orElseThrow());
        assertEquals("minecraft:wither_skeleton_skull", Values.lookup("cabeça de whiter").orElseThrow());
        assertEquals("minecraft:hay_block", Values.lookup("fenos").orElseThrow());
        assertTrue(Values.lookup("espada de netherite lendária").isEmpty(), "nome desconhecido nao vira item");
        assertEquals("maçãs", Values.name("minecraft:apple", true));
    }

    @Test
    void spokenNamesMatchEvenWithoutDe() {
        assertEquals("minecraft:wither_skeleton_skull", Values.lookup("cabeça Wither").orElse(""));
        assertEquals("minecraft:wither_skeleton_skull", Values.lookup("3 cabeças wither").orElse(""));
        assertEquals("minecraft:wither_skeleton_skull", Values.lookup("cabeça de esqueleto wither").orElse(""));
        assertTrue(Values.lookup("coisa que não existe").isEmpty());
    }
}
