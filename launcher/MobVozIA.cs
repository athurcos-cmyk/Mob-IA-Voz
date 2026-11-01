// MobVoz IA: programa de instalacao e controle (Windows Forms). Compila com o csc.exe que ja vem no Windows (C# 5, .NET Framework 4).
// Faz o que os .bat faziam, numa janela: instalar/atualizar, guardar a chave da API UMA vez, ligar/parar o servico de IA e abrir o Minecraft.
// O programa NAO instala nada por conta propria: chama os mesmos scripts do pacote (componentes\instalar-tudo.ps1 etc.), que sao abertos e legiveis.
// Opcoes de linha de comando (usadas nos testes): --status | --salvar-chave <chave> | --remover-chave | --testar-chave | --versao
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net;
using System.Text;
using System.Threading;
using System.Windows.Forms;

namespace MobVozIA
{
    // ------------------------------------------------------------------------------------------------ caminhos e estado (sem janela)
    static class Caminhos
    {
        public static readonly string Exe = AppDomain.CurrentDomain.BaseDirectory.TrimEnd('\\');
        // o pacote guarda tudo em "componentes"; durante o desenvolvimento os arquivos ficam ao lado do exe
        public static string Comp
        {
            get { string c = Path.Combine(Exe, "componentes"); return Directory.Exists(c) ? c : Exe; }
        }
        public static string Home
        {
            get
            {
                string h = Environment.GetEnvironmentVariable("MOBVOZ_HOME");
                return string.IsNullOrEmpty(h) ? Path.Combine(Environment.GetEnvironmentVariable("USERPROFILE") ?? "", ".mobvoz") : h;
            }
        }
        public static string Fixa { get { return Path.Combine(Home, "sidecar"); } }
        public static string VenvPython { get { return Path.Combine(Fixa, "venv\\Scripts\\python.exe"); } }
        public static string Modelos { get { return Path.Combine(Fixa, "models"); } }
        public static string ArqChave { get { return Path.Combine(Home, "api.json"); } }
        public static string ArqProvedor { get { return Path.Combine(Home, "provedor.txt"); } }
        public static string PastaJogo
        {
            get { return Path.Combine(Environment.GetEnvironmentVariable("USERPROFILE") ?? "", "MobVozJogo"); }
        }
        public static string Versao()
        {
            try
            {
                foreach (string f in Directory.GetFiles(Path.Combine(Comp, "mod"), "mobvoz-*.jar"))
                {
                    string n = Path.GetFileNameWithoutExtension(f);
                    return n.Substring("mobvoz-".Length);
                }
            }
            catch (Exception) { }
            return "?";
        }
    }

    static class Estado
    {
        public static bool ModsInstalados()
        {
            try { return Directory.GetFiles(Path.Combine(Caminhos.PastaJogo, "mods"), "mobvoz-*.jar").Length > 0; } catch (Exception) { return false; }
        }
        public static bool ServicoInstalado()
        {
            return File.Exists(Caminhos.VenvPython) && File.Exists(Path.Combine(Caminhos.Modelos, "pt_BR-faber-medium.onnx"));
        }
        public static string Chave()
        {
            try
            {
                string t = File.ReadAllText(Caminhos.ArqChave, Encoding.UTF8).TrimStart('﻿');
                int i = t.IndexOf("\"key\"", StringComparison.Ordinal);
                if (i < 0) return "";
                int a = t.IndexOf('"', t.IndexOf(':', i) + 1);
                int b = a < 0 ? -1 : t.IndexOf('"', a + 1);
                while (b > 0 && t[b - 1] == '\\') b = t.IndexOf('"', b + 1);
                return (a >= 0 && b > a) ? t.Substring(a + 1, b - a - 1).Replace("\\\"", "\"").Replace("\\\\", "\\").Trim() : "";
            }
            catch (Exception) { return ""; }
        }
        public static string ChaveMascarada()
        {
            string k = Chave();
            return k.Length < 8 ? "" : "••••••••" + k.Substring(k.Length - 4);
        }
        public static void SalvarChave(string chave)
        {
            Directory.CreateDirectory(Caminhos.Home);
            string json = "{\"key\": \"" + chave.Replace("\\", "\\\\").Replace("\"", "\\\"") + "\"}";
            File.WriteAllText(Caminhos.ArqChave, json, new UTF8Encoding(false)); // sem BOM: o servico le sem problema
        }
        public static void RemoverChave() { try { File.Delete(Caminhos.ArqChave); } catch (Exception) { } }
        public static string Provedor()
        {
            try { string p = File.ReadAllText(Caminhos.ArqProvedor).Trim(); if (p == "api" || p == "local") return p; } catch (Exception) { }
            return Chave().Length > 0 ? "api" : "local";
        }
        public static void SalvarProvedor(string p)
        {
            Directory.CreateDirectory(Caminhos.Home);
            File.WriteAllText(Caminhos.ArqProvedor, p, new UTF8Encoding(false));
        }

        /// <summary>Testa a chave na API (GET /models). Devolve: "ok", "recusada" ou "sem-teste: motivo".</summary>
        public static string TestarChave(string chave)
        {
            try
            {
                ServicePointManager.SecurityProtocol = (SecurityProtocolType)3072; // TLS 1.2
                string baseUrl = Environment.GetEnvironmentVariable("MOBVOZ_API_URL");
                if (string.IsNullOrEmpty(baseUrl)) baseUrl = "https://api.deepseek.com";
                HttpWebRequest rq = (HttpWebRequest)WebRequest.Create(baseUrl.TrimEnd('/') + "/models");
                rq.Headers["Authorization"] = "Bearer " + chave;
                rq.Timeout = 12000;
                rq.UserAgent = "MobVozIA-programa/0.1";
                using (HttpWebResponse rs = (HttpWebResponse)rq.GetResponse()) { return rs.StatusCode == HttpStatusCode.OK ? "ok" : "sem-teste: HTTP " + (int)rs.StatusCode; }
            }
            catch (WebException e)
            {
                HttpWebResponse r = e.Response as HttpWebResponse;
                if (r != null && (r.StatusCode == HttpStatusCode.Unauthorized || r.StatusCode == HttpStatusCode.Forbidden)) return "recusada";
                return "sem-teste: " + (r != null ? "HTTP " + (int)r.StatusCode : e.Status.ToString());
            }
            catch (Exception e) { return "sem-teste: " + e.GetType().Name; }
        }

        public static bool ServicoOnline()
        {
            try
            {
                HttpWebRequest rq = (HttpWebRequest)WebRequest.Create("http://127.0.0.1:8765/health");
                rq.Timeout = 3000; // o /health consulta o Ollama: com 1 s a conexao era abortada e o servico imprimia um erro no registro
                using (HttpWebResponse rs = (HttpWebResponse)rq.GetResponse()) { return rs.StatusCode == HttpStatusCode.OK; }
            }
            catch (Exception) { return false; }
        }
    }

    // ------------------------------------------------------------------------------------------------ entrada
    static class Programa
    {
        [STAThread]
        static int Main(string[] args)
        {
            if (args.Length > 0 && args[0].StartsWith("--"))
            {
                return Cli(args);
            }
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new Janela());
            return 0;
        }

        static int Cli(string[] a)
        {
            switch (a[0])
            {
                case "--versao": Console.WriteLine(Caminhos.Versao()); return 0;
                case "--status":
                    Console.WriteLine("versao=" + Caminhos.Versao());
                    Console.WriteLine("componentes=" + Caminhos.Comp);
                    Console.WriteLine("mods=" + Estado.ModsInstalados());
                    Console.WriteLine("servico_instalado=" + Estado.ServicoInstalado());
                    Console.WriteLine("chave=" + (Estado.Chave().Length > 0 ? Estado.ChaveMascarada() : "nenhuma"));
                    Console.WriteLine("provedor=" + Estado.Provedor());
                    Console.WriteLine("servico_online=" + Estado.ServicoOnline());
                    return 0;
                case "--salvar-chave":
                    if (a.Length < 2 || a[1].Trim().Length < 16) { Console.WriteLine("chave curta demais"); return 2; }
                    Estado.SalvarChave(a[1].Trim()); Console.WriteLine("salva: " + Estado.ChaveMascarada()); return 0;
                case "--remover-chave": Estado.RemoverChave(); Console.WriteLine("removida"); return 0;
                case "--testar-chave": Console.WriteLine(Estado.TestarChave(Estado.Chave())); return 0;
            }
            Console.WriteLine("opcoes: --status --salvar-chave <chave> --remover-chave --testar-chave --versao");
            return 1;
        }
    }

    // ------------------------------------------------------------------------------------------------ janela
    class Janela : Form
    {
        static readonly Color Fundo = Color.FromArgb(13, 17, 15), Cartao = Color.FromArgb(23, 31, 25), Linha = Color.FromArgb(43, 56, 46),
            Texto = Color.FromArgb(234, 241, 234), Suave = Color.FromArgb(157, 176, 161), Verde = Color.FromArgb(58, 138, 48),
            VerdeVivo = Color.FromArgb(52, 224, 122), Ouro = Color.FromArgb(247, 201, 72), Vermelho = Color.FromArgb(238, 90, 69);

        Label lbMods, lbServ, lbIa, lbOn;
        Panel dotMods, dotServ, dotIa, dotOn;
        Button btInstalar, btSalvar, btRemover, btLigar, btParar, btOllama;
        CheckBox cbLeve;
        RadioButton rbApi, rbLocal;
        TextBox tbChave, tbLog;
        Label lbChave, lbNotaLocal;
        ProgressBar barra;
        System.Windows.Forms.Timer tempo = new System.Windows.Forms.Timer();
        Process instalacao, servico;
        bool consultando;

        public Janela()
        {
            Text = "MobVoz IA " + Caminhos.Versao();
            Font = new Font("Segoe UI", 9.5f);
            BackColor = Fundo;
            ForeColor = Texto;
            ClientSize = new Size(760, 760);
            MinimumSize = new Size(700, 600);
            StartPosition = FormStartPosition.CenterScreen;
            try { Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath); } catch (Exception) { }

            FlowLayoutPanel raiz = new FlowLayoutPanel();
            raiz.Dock = DockStyle.Fill;
            raiz.FlowDirection = FlowDirection.TopDown;
            raiz.WrapContents = false;
            raiz.AutoScroll = true;
            raiz.Padding = new Padding(20, 16, 20, 16);
            Controls.Add(raiz);

            Label t = new Label();
            t.Text = "MobVoz IA";
            t.Font = new Font("Segoe UI Semibold", 22f);
            t.ForeColor = Texto;
            t.AutoSize = true;
            raiz.Controls.Add(t);
            Label sub = Rotulo("Fale com os mobs do Minecraft. Versao " + Caminhos.Versao() + " (Alpha). Tudo roda no seu computador.", Suave);
            raiz.Controls.Add(sub);

            // ---- estado
            Panel pe = Cartao_("Estado");
            AddLinha(pe, 0, out dotMods, out lbMods);
            AddLinha(pe, 1, out dotServ, out lbServ);
            AddLinha(pe, 2, out dotIa, out lbIa);
            AddLinha(pe, 3, out dotOn, out lbOn);
            pe.Height = 150;
            raiz.Controls.Add(pe);

            // ---- instalar
            Panel pi = Cartao_("1. Instalar ou atualizar");
            btInstalar = Botao("Instalar / atualizar tudo", Verde, 220);
            btInstalar.Location = new Point(14, 36);
            btInstalar.Click += delegate { Instalar(); };
            pi.Controls.Add(btInstalar);
            cbLeve = new CheckBox();
            cbLeve.Text = "Modo leve (so a voz Piper; para PC fraco)";
            cbLeve.AutoSize = true;
            cbLeve.ForeColor = Suave;
            cbLeve.Location = new Point(250, 44);
            pi.Controls.Add(cbLeve);
            Label dicaI = Rotulo("Instala o Python, o Fabric, os 3 mods e o servico de IA (voz e fala). Se ja estiver tudo pronto, so confere (segundos).", Suave);
            dicaI.Location = new Point(14, 78);
            dicaI.MaximumSize = new Size(690, 0);
            pi.Controls.Add(dicaI);
            barra = new ProgressBar();
            barra.Style = ProgressBarStyle.Marquee;
            barra.MarqueeAnimationSpeed = 30;
            barra.Visible = false;
            barra.Location = new Point(14, 112);
            barra.Size = new Size(690, 10);
            pi.Controls.Add(barra);
            pi.Height = 135;
            raiz.Controls.Add(pi);

            // ---- IA
            Panel pa = Cartao_("2. IA que conversa");
            rbApi = new RadioButton();
            rbApi.Text = "Minha API (recomendado: melhor e nao pesa no PC)";
            rbApi.AutoSize = true;
            rbApi.Location = new Point(14, 34);
            rbApi.CheckedChanged += delegate { TrocouProvedor(); };
            pa.Controls.Add(rbApi);
            tbChave = new TextBox();
            tbChave.UseSystemPasswordChar = true;
            tbChave.Location = new Point(34, 62);
            tbChave.Size = new Size(380, 26);
            tbChave.BackColor = Color.FromArgb(10, 14, 11);
            tbChave.ForeColor = Texto;
            tbChave.BorderStyle = BorderStyle.FixedSingle;
            pa.Controls.Add(tbChave);
            btSalvar = Botao("Salvar e testar", Verde, 130);
            btSalvar.Location = new Point(424, 59);
            btSalvar.Click += delegate { SalvarChave(); };
            pa.Controls.Add(btSalvar);
            btRemover = Botao("Remover", Color.FromArgb(70, 40, 38), 90);
            btRemover.Location = new Point(562, 59);
            btRemover.Click += delegate { RemoverChave(); };
            pa.Controls.Add(btRemover);
            lbChave = Rotulo("", Suave);
            lbChave.Location = new Point(34, 94);
            lbChave.MaximumSize = new Size(670, 0);
            pa.Controls.Add(lbChave);
            rbLocal = new RadioButton();
            rbLocal.Text = "Modelo local gratis (Ollama; pesa mais e e menos esperto)";
            rbLocal.AutoSize = true;
            rbLocal.Location = new Point(14, 128);
            pa.Controls.Add(rbLocal);
            btOllama = Botao("Baixar o modelo (qwen3:4b)", Color.FromArgb(50, 62, 54), 200);
            btOllama.Location = new Point(34, 156);
            btOllama.Click += delegate { BaixarModelo(); };
            pa.Controls.Add(btOllama);
            lbNotaLocal = Rotulo("Precisa do Ollama instalado (ollama.com/download).", Suave);
            lbNotaLocal.Location = new Point(246, 162);
            lbNotaLocal.Cursor = Cursors.Hand;
            lbNotaLocal.Click += delegate { Abrir("https://ollama.com/download"); };
            pa.Controls.Add(lbNotaLocal);
            pa.Height = 196;
            raiz.Controls.Add(pa);

            // ---- servico
            Panel ps = Cartao_("3. Jogar");
            btLigar = Botao("Ligar o servico de IA", Verde, 200);
            btLigar.Location = new Point(14, 36);
            btLigar.Click += delegate { Ligar(); };
            ps.Controls.Add(btLigar);
            btParar = Botao("Parar", Color.FromArgb(70, 40, 38), 90);
            btParar.Location = new Point(224, 36);
            btParar.Click += delegate { Parar(); };
            ps.Controls.Add(btParar);
            Button btMc = Botao("Abrir o Minecraft", Color.FromArgb(50, 62, 54), 150);
            btMc.Location = new Point(326, 36);
            btMc.Click += delegate { AbrirMinecraft(); };
            ps.Controls.Add(btMc);
            Button btPasta = Botao("Pasta do jogo", Color.FromArgb(50, 62, 54), 120);
            btPasta.Location = new Point(486, 36);
            btPasta.Click += delegate { Abrir(Caminhos.PastaJogo); };
            ps.Controls.Add(btPasta);
            Label dicaS = Rotulo("Deixe o servico ligado enquanto joga. No jogo: /mobvoz voz sim para o microfone, /mobvoz ajuda para tudo o que da para fazer.", Suave);
            dicaS.Location = new Point(14, 82);
            dicaS.MaximumSize = new Size(690, 0);
            ps.Controls.Add(dicaS);
            ps.Height = 118;
            raiz.Controls.Add(ps);

            // ---- registro
            Label lg = Rotulo("Registro", Suave);
            raiz.Controls.Add(lg);
            tbLog = new TextBox();
            tbLog.Multiline = true;
            tbLog.ReadOnly = true;
            tbLog.ScrollBars = ScrollBars.Vertical;
            tbLog.Font = new Font("Consolas", 9f);
            tbLog.BackColor = Color.FromArgb(7, 10, 8);
            tbLog.ForeColor = Color.FromArgb(201, 242, 211);
            tbLog.BorderStyle = BorderStyle.FixedSingle;
            tbLog.Size = new Size(700, 150);
            raiz.Controls.Add(tbLog);

            Load += delegate
            {
                string p = Estado.Provedor();
                rbApi.Checked = p == "api";
                rbLocal.Checked = p == "local";
                TrocouProvedor();
                Atualizar();
                tempo.Interval = 2500;
                tempo.Tick += delegate { Atualizar(); };
                tempo.Start();
                Log("Pronto. Pasta de dados: " + Caminhos.Home);
            };
            FormClosing += delegate(object s, FormClosingEventArgs e)
            {
                if (instalacao != null && !instalacao.HasExited)
                {
                    if (MessageBox.Show("A instalacao ainda esta rodando. Fechar mesmo assim?", "MobVoz IA", MessageBoxButtons.YesNo, MessageBoxIcon.Warning) != DialogResult.Yes) { e.Cancel = true; return; }
                    try { instalacao.Kill(); } catch (Exception) { }
                }
                if (servico != null && !servico.HasExited)
                {
                    try { servico.Kill(); } catch (Exception) { }
                }
            };
        }

        // ---------------------------------------------------------------- pecas de interface
        Label Rotulo(string texto, Color cor)
        {
            Label l = new Label();
            l.Text = texto;
            l.ForeColor = cor;
            l.AutoSize = true;
            return l;
        }

        Button Botao(string texto, Color cor, int largura)
        {
            Button b = new Button();
            b.Text = texto;
            b.FlatStyle = FlatStyle.Flat;
            b.FlatAppearance.BorderColor = Linha;
            b.FlatAppearance.BorderSize = 1;
            b.BackColor = cor;
            b.ForeColor = Color.White;
            b.Font = new Font("Segoe UI Semibold", 9.5f);
            b.Size = new Size(largura, 34);
            b.Cursor = Cursors.Hand;
            b.UseVisualStyleBackColor = false;
            return b;
        }

        Panel Cartao_(string titulo)
        {
            Panel p = new Panel();
            p.BackColor = Cartao;
            p.Width = 718;
            p.Margin = new Padding(0, 10, 0, 0);
            Label l = new Label();
            l.Text = titulo;
            l.Font = new Font("Segoe UI Semibold", 10.5f);
            l.ForeColor = VerdeVivo;
            l.AutoSize = true;
            l.Location = new Point(14, 8);
            p.Controls.Add(l);
            p.Paint += delegate(object s, PaintEventArgs e) { e.Graphics.DrawRectangle(new Pen(Linha), 0, 0, p.Width - 1, p.Height - 1); };
            return p;
        }

        void AddLinha(Panel p, int i, out Panel dot, out Label lb)
        {
            dot = new Panel();
            dot.Size = new Size(12, 12);
            dot.Location = new Point(16, 40 + i * 26);
            dot.BackColor = Suave;
            p.Controls.Add(dot);
            lb = new Label();
            lb.AutoSize = true;
            lb.Location = new Point(36, 36 + i * 26);
            lb.ForeColor = Texto;
            p.Controls.Add(lb);
        }

        void Pintar(Panel dot, Label lb, bool ok, string texto, bool aviso)
        {
            dot.BackColor = ok ? VerdeVivo : (aviso ? Ouro : Vermelho);
            lb.Text = texto;
        }

        // ---------------------------------------------------------------- estado
        void Atualizar()
        {
            bool mods = Estado.ModsInstalados();
            Pintar(dotMods, lbMods, mods, mods ? "Minecraft com Fabric e os 3 mods: instalados" : "Minecraft e mods: ainda nao instalados (clique em Instalar)", !mods);
            bool serv = Estado.ServicoInstalado();
            Pintar(dotServ, lbServ, serv, serv ? "Servico de IA (voz e reconhecimento de fala): instalado" : "Servico de IA: ainda nao instalado (clique em Instalar)", !serv);
            string p = rbApi.Checked ? "api" : "local";
            string chave = Estado.ChaveMascarada();
            if (p == "api") Pintar(dotIa, lbIa, chave.Length > 0, chave.Length > 0 ? "IA que conversa: sua API (chave " + chave + ")" : "IA que conversa: falta guardar a chave da API", chave.Length == 0);
            else Pintar(dotIa, lbIa, true, "IA que conversa: modelo local (Ollama)", false);
            lbChave.Text = chave.Length > 0 ? "Chave guardada neste computador: " + chave + ". Fica salva: nao precisa colar de novo, nem ao atualizar o MobVoz." : "Nenhuma chave guardada. Cole a chave da sua API (ex.: DeepSeek) e clique em Salvar e testar.";
            bool rodando = servico != null && !servico.HasExited;
            btLigar.Enabled = !rodando;
            btParar.Enabled = rodando;
            if (!consultando)
            {
                consultando = true;
                ThreadPool.QueueUserWorkItem(delegate
                {
                    bool on = Estado.ServicoOnline();
                    try { BeginInvoke((MethodInvoker)delegate { Pintar(dotOn, lbOn, on, on ? "Servico de IA: ligado e respondendo" : "Servico de IA: desligado (clique em Ligar antes de jogar)", !on); consultando = false; }); }
                    catch (Exception) { consultando = false; }
                });
            }
        }

        void Log(string linha)
        {
            if (IsDisposed) return;
            if (InvokeRequired) { try { BeginInvoke((MethodInvoker)delegate { Log(linha); }); } catch (Exception) { } return; }
            tbLog.AppendText(linha + Environment.NewLine);
        }

        void Ocupado(bool sim)
        {
            btInstalar.Enabled = !sim;
            barra.Visible = sim;
        }

        // ---------------------------------------------------------------- acoes
        void Instalar()
        {
            string script = Path.Combine(Caminhos.Comp, "instalar-tudo.ps1");
            if (!File.Exists(script)) { MessageBox.Show("Nao achei o instalador (" + script + "). Extraia o pacote inteiro antes de abrir este programa.", "MobVoz IA"); return; }
            tbLog.Clear();
            Ocupado(true);
            Log("Instalando... (pode levar alguns minutos na primeira vez; depois e rapido)");
            instalacao = Rodar("powershell.exe", "-NoProfile -ExecutionPolicy Bypass -File \"" + script + "\"" + (cbLeve.Checked ? " -Leve" : ""), Caminhos.Comp, null, delegate(int codigo)
            {
                BeginInvoke((MethodInvoker)delegate
                {
                    Ocupado(false);
                    Log(codigo == 0 ? "== Instalacao concluida." : "== A instalacao terminou com erro (codigo " + codigo + "). Leia as ultimas linhas acima; pode clicar de novo, ele continua de onde parou.");
                    Atualizar();
                    if (codigo == 0) MessageBox.Show("Tudo pronto! Agora escolha a IA que conversa (passo 2) e ligue o servico (passo 3).", "MobVoz IA", MessageBoxButtons.OK, MessageBoxIcon.Information);
                });
            });
        }

        Process Rodar(string exe, string args, string pasta, Dictionary<string, string> ambiente, Action<int> aoTerminar)
        {
            ProcessStartInfo psi = new ProcessStartInfo(exe, args);
            psi.WorkingDirectory = pasta;
            psi.UseShellExecute = false;
            psi.CreateNoWindow = true;
            psi.RedirectStandardOutput = true;
            psi.RedirectStandardError = true;
            psi.StandardOutputEncoding = Encoding.UTF8;
            psi.StandardErrorEncoding = Encoding.UTF8;
            psi.EnvironmentVariables["PYTHONIOENCODING"] = "utf-8";
            psi.EnvironmentVariables["HF_HUB_DISABLE_SYMLINKS_WARNING"] = "1";
            if (ambiente != null) foreach (KeyValuePair<string, string> kv in ambiente) psi.EnvironmentVariables[kv.Key] = kv.Value;
            Process p = new Process();
            p.StartInfo = psi;
            p.EnableRaisingEvents = true;
            p.OutputDataReceived += delegate(object s, DataReceivedEventArgs e) { if (e.Data != null && !Ruido(e.Data)) Log(e.Data); };
            p.ErrorDataReceived += delegate(object s, DataReceivedEventArgs e) { if (e.Data != null && !Ruido(e.Data)) Log(e.Data); };
            if (aoTerminar != null) p.Exited += delegate { int c; try { c = p.ExitCode; } catch (Exception) { c = -1; } aoTerminar(c); };
            try { p.Start(); p.BeginOutputReadLine(); p.BeginErrorReadLine(); }
            catch (Exception e) { Log("Nao consegui iniciar " + exe + ": " + e.Message); if (aoTerminar != null) aoTerminar(-1); return null; }
            return p;
        }

        // o programa consulta o /health a cada poucos segundos: essas linhas so enchem o registro
        static bool Ruido(string linha) { return linha.Contains("mobvoz.http GET /health"); }

        void TrocouProvedor()
        {
            bool api = rbApi.Checked;
            tbChave.Enabled = api;
            btSalvar.Enabled = api;
            btRemover.Enabled = api;
            btOllama.Enabled = !api;
            Estado.SalvarProvedor(api ? "api" : "local");
            Atualizar();
        }

        void SalvarChave()
        {
            string k = tbChave.Text.Trim();
            if (k.Length < 16) { MessageBox.Show("Essa chave parece curta demais. Confira e cole de novo.", "MobVoz IA", MessageBoxButtons.OK, MessageBoxIcon.Warning); return; }
            Estado.SalvarChave(k);
            tbChave.Clear();
            btSalvar.Enabled = false;
            Log("Chave guardada. Testando na API...");
            string chave = k;
            ThreadPool.QueueUserWorkItem(delegate
            {
                string r = Estado.TestarChave(chave);
                BeginInvoke((MethodInvoker)delegate
                {
                    btSalvar.Enabled = true;
                    if (r == "ok") { Log("Chave valida."); MessageBox.Show("Chave guardada e funcionando!", "MobVoz IA", MessageBoxButtons.OK, MessageBoxIcon.Information); }
                    else if (r == "recusada") { Log("A API recusou a chave."); MessageBox.Show("A API recusou essa chave (confira se copiou inteira e se a conta tem credito). Ela ficou guardada; cole outra para trocar.", "MobVoz IA", MessageBoxButtons.OK, MessageBoxIcon.Warning); }
                    else { Log("Nao consegui testar (" + r + "). A chave ficou guardada."); MessageBox.Show("Chave guardada, mas nao consegui testar agora (" + r + "). Sem internet? Ela sera usada quando ligar o servico.", "MobVoz IA"); }
                    Atualizar();
                });
            });
        }

        void RemoverChave()
        {
            if (Estado.Chave().Length == 0) return;
            if (MessageBox.Show("Apagar a chave guardada neste computador?", "MobVoz IA", MessageBoxButtons.YesNo, MessageBoxIcon.Question) != DialogResult.Yes) return;
            Estado.RemoverChave();
            Log("Chave removida.");
            Atualizar();
        }

        void BaixarModelo()
        {
            Ocupado(true);
            Log("Baixando o modelo local (cerca de 2,6 GB)...");
            Process p = Rodar("cmd.exe", "/c ollama pull qwen3:4b", Caminhos.Home.Length > 0 && Directory.Exists(Caminhos.Home) ? Caminhos.Home : Caminhos.Exe, null, delegate(int c)
            {
                BeginInvoke((MethodInvoker)delegate
                {
                    Ocupado(false);
                    Log(c == 0 ? "== Modelo pronto." : "== Nao deu certo (o Ollama esta instalado e aberto?). Instale em ollama.com/download e tente de novo.");
                });
            });
        }

        void Ligar()
        {
            if (!Estado.ServicoInstalado()) { MessageBox.Show("O servico de IA ainda nao esta instalado. Clique em Instalar / atualizar tudo.", "MobVoz IA"); return; }
            bool api = rbApi.Checked;
            if (api && Estado.Chave().Length == 0) { MessageBox.Show("Guarde a chave da sua API primeiro (passo 2), ou escolha o modelo local.", "MobVoz IA"); return; }
            if (Estado.ServicoOnline()) { Log("O servico ja esta ligado."); Atualizar(); return; }
            Dictionary<string, string> env = new Dictionary<string, string>();
            env["MOBVOZ_MODELS"] = Caminhos.Modelos;
            env["MOBVOZ_PROVIDER"] = api ? "api" : "ollama";
            Log("Ligando o servico de IA (" + (api ? "sua API" : "modelo local") + "). A primeira fala demora uns 10 a 15 s (carrega os modelos).");
            servico = Rodar(Caminhos.VenvPython, "-m app.api", Path.Combine(Caminhos.Comp, "ai-service"), env, delegate(int c)
            {
                BeginInvoke((MethodInvoker)delegate { Log("== Servico parou (codigo " + c + ")."); Atualizar(); });
            });
            Atualizar();
        }

        void Parar()
        {
            try { if (servico != null && !servico.HasExited) servico.Kill(); } catch (Exception) { }
            Log("Servico parado.");
            Atualizar();
        }

        void AbrirMinecraft()
        {
            string[] cands = {
                Path.Combine(Environment.GetEnvironmentVariable("ProgramFiles(x86)") ?? "", "Minecraft Launcher\\MinecraftLauncher.exe"),
                Path.Combine(Environment.GetEnvironmentVariable("ProgramFiles") ?? "", "Minecraft Launcher\\MinecraftLauncher.exe"),
            };
            foreach (string c in cands) if (File.Exists(c)) { Abrir(c); return; }
            try { Process.Start("explorer.exe", "shell:AppsFolder\\Microsoft.4297127D64EC6_8wekyb3d8bbwe!Minecraft"); return; } catch (Exception) { }
            MessageBox.Show("Nao achei o Minecraft Launcher. Abra-o pelo menu Iniciar e escolha a instalacao \"MobVoz IA\".", "MobVoz IA");
        }

        void Abrir(string alvo)
        {
            try { Process.Start(new ProcessStartInfo(alvo) { UseShellExecute = true }); }
            catch (Exception e) { Log("Nao consegui abrir " + alvo + ": " + e.Message); }
        }
    }
}
