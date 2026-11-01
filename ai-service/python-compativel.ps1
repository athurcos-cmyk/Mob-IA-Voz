# Escolhe um Python que as bibliotecas de voz aceitam (3.10 a 3.14). Python novo demais (ex.: 3.15) NAO serve: faltam pacotes prontos (onnxruntime).
# Usado por instalar-sidecar.ps1 e pelo 1-INSTALAR.bat. So ASCII (o PowerShell 5 nao le acento sem BOM).
$script:PyMin = [version]'3.10'
$script:PyMax = [version]$(if ($env:MOBVOZ_PY_MAX) { $env:MOBVOZ_PY_MAX } else { '3.14' })

function Versao-Python($exe) {
    try { return [version]((& $exe -c "import sys; print('%d.%d' % sys.version_info[:2])" 2>$null) | Select-Object -First 1).Trim() } catch { return $null }
}

# devolve @{ Exe = ...; Ver = ... } ou $null
function Achar-Python {
    $cands = @()
    if ($env:MOBVOZ_PYTHON -and (Test-Path $env:MOBVOZ_PYTHON)) { $cands += $env:MOBVOZ_PYTHON }
    # ignora o "atalho" da Microsoft Store (WindowsApps), que abre a loja em vez de rodar o Python
    $c = Get-Command python -ErrorAction SilentlyContinue
    if ($c -and $c.Source -notlike '*WindowsApps*') { $cands += $c.Source }
    if (Get-Command py -ErrorAction SilentlyContinue) {
        foreach ($m in 14, 13, 12, 11, 10) {
            try { $e = (& py "-3.$m" -c "import sys; print(sys.executable)" 2>$null | Select-Object -First 1); if ($e) { $cands += $e.Trim() } } catch { }
        }
    }
    $cands += Get-ChildItem "$env:LOCALAPPDATA\Programs\Python\Python3*\python.exe" -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | ForEach-Object FullName
    $cands += Get-ChildItem "$env:ProgramFiles\Python3*\python.exe" -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | ForEach-Object FullName
    foreach ($p in ($cands | Select-Object -Unique)) {
        $v = Versao-Python $p
        if ($v -and $v -ge $script:PyMin -and $v -le $script:PyMax) { return @{ Exe = $p; Ver = $v } }
    }
    return $null
}
