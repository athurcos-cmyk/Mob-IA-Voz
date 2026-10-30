# MobVoz IA — fase 0

Base compilável do mod para Minecraft Java **1.21.8**, Fabric e **JDK 21**. O mod apenas registra uma mensagem de inicialização. Voz, IA, alvos, crenças, alianças e ações de jogo pertencem às fases seguintes.

## Versões fixadas

| Componente | Versão |
|---|---|
| Minecraft | 1.21.8 |
| Java para o build | 21 |
| Gradle Wrapper | 8.14.3 |
| Fabric Loom | 1.11.8 |
| Fabric Loader | 0.17.3 |
| Fabric API | 0.136.1+1.21.8 |
| Mapeamentos | Oficiais da Mojang para 1.21.8 |

O Gradle Wrapper usa SHA-256 fixado. O JDK portátil em `../.tools/` foi usado no primeiro build local; essa pasta é ignorada pelo Git. Em outro computador, instale o JDK 21 e defina `JAVA_HOME` se não tiver a cópia portátil.

Versões e compatibilidade foram conferidas no [Maven oficial do Fabric](https://maven.fabricmc.net/), na [documentação e releases do Loom](https://github.com/FabricMC/fabric-loom/releases), na [matriz de Java do Gradle](https://docs.gradle.org/current/userguide/compatibility.html) e no [checksum oficial do Gradle 8.14.3](https://services.gradle.org/distributions/gradle-8.14.3-bin.zip.sha256). O JDK portátil é o [Temurin 21](https://adoptium.net/temurin/releases/?version=21).

## Build e diagnóstico no Windows

No PowerShell, dentro de `mod-fabric/`:

```powershell
.\diagnose.bat
.\build.bat
```

Ou use o wrapper diretamente com `JAVA_HOME` apontando para o JDK 21:

```powershell
.\gradlew.bat build
```

`build.bat` configura um cache Gradle local em `.gradle-user-home/`. O JAR final fica em `build/libs/mobvoz-0.0.1.jar`; o JAR `-sources` é apenas para desenvolvimento. O build roda a tarefa `verifyModMetadata` por meio de `check`.

## Teste manual T01 em perfil isolado

1. Execute `.\run-client.bat`. O Loom usa o diretório de jogo `mod-fabric/run/`, separado de `%APPDATA%\.minecraft` e de outros perfis.
2. Aguarde o Minecraft abrir e confirme que a versão é 1.21.8 com Fabric, que `MobVoz IA` aparece na lista de mods (se disponível) e que o log mostra `MobVoz IA: base Fabric carregada (fase 0).`.
3. Feche o jogo normalmente e verifique se não houve crash. Não copie o JAR para a instalação global `.minecraft/mods`.

Para testar pelo Launcher em vez do Loom, crie uma instalação Fabric 1.21.8 com **diretório de jogo exclusivo** e Java 21. Instale o Fabric API compatível nesse perfil e copie apenas `mobvoz-0.0.1.jar` para o `mods/` desse diretório. A fase 0 só fica validada quando esse teste manual passar no computador do usuário.

