/* MobVoz IA: interacoes da pagina (sem dependencias). Tudo que usa localStorage e opcional e protegido. */
(function () {
  "use strict";

  const $ = (s, r) => (r || document).querySelector(s);
  const $$ = (s, r) => Array.from((r || document).querySelectorAll(s));

  /* ---- armazenamento seguro ---- */
  const store = {
    get(k) { try { return window.localStorage.getItem(k); } catch (e) { return null; } },
    set(k, v) { try { window.localStorage.setItem(k, v); } catch (e) { /* sem armazenamento: segue sem salvar */ }
    }
  };

  /* ---- toast de conquista ---- */
  const toast = $("#toast");
  let toastTimer;
  function conquista(txt) {
    if (!toast) return;
    $("#toast-txt", toast).textContent = txt;
    toast.classList.add("show");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove("show"), 4200);
  }

  /* ---- navegacao: destaca a secao visivel ---- */
  const links = $$(".hotbar a");
  const byId = new Map(links.map((a) => [a.getAttribute("href").slice(1), a]));
  if ("IntersectionObserver" in window) {
    const io = new IntersectionObserver((entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting && byId.has(e.target.id)) {
          links.forEach((l) => l.classList.remove("active"));
          const a = byId.get(e.target.id);
          a.classList.add("active");
          a.scrollIntoView({ block: "nearest", inline: "center" });
        }
      });
    }, { rootMargin: "-45% 0px -50% 0px" });
    byId.forEach((_, id) => { const el = document.getElementById(id); if (el) io.observe(el); });
  }

  /* ---- botoes de copiar ---- */
  $$(".code").forEach((box) => {
    const btn = $(".copy", box);
    const pre = $("pre", box);
    if (!btn || !pre) return;
    btn.addEventListener("click", async () => {
      const txt = pre.innerText.trim();
      try {
        await navigator.clipboard.writeText(txt);
        btn.textContent = "Copiado!";
      } catch (e) {
        const r = document.createRange();
        r.selectNodeContents(pre);
        const sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(r);
        btn.textContent = "Selecionado: Ctrl+C";
      }
      setTimeout(() => { btn.textContent = "Copiar"; }, 1800);
    });
  });

  /* ---- checklist de instalacao ---- */
  const steps = $$("#steps .step");
  const bar = $("#prog-bar");
  const txt = $("#prog-txt");
  const KEY = "mobvoz.passos";
  let saved = [];
  try { saved = JSON.parse(store.get(KEY) || "[]"); } catch (e) { saved = []; }

  function update(announce) {
    const done = steps.filter((s) => s.classList.contains("done")).length;
    if (bar) bar.style.width = (done / steps.length * 100) + "%";
    if (txt) txt.textContent = done + " de " + steps.length + " passos";
    store.set(KEY, JSON.stringify(steps.map((s, i) => s.classList.contains("done") ? i + 1 : 0).filter(Boolean)));
    if (announce) {
      if (done === steps.length) conquista("Instalou o MobVoz! Agora é só conversar com os mobs.");
      else if (done === 1) conquista("Primeiro passo concluído");
      else if (done === Math.ceil(steps.length / 2)) conquista("Metade do caminho");
    }
  }
  steps.forEach((s, i) => {
    const cb = $("input[type=checkbox]", s);
    if (!cb) return;
    if (saved.includes(i + 1)) { cb.checked = true; s.classList.add("done"); }
    cb.addEventListener("change", () => {
      s.classList.toggle("done", cb.checked);
      update(cb.checked);
    });
  });
  update(false);

  /* ---- abas (API / local) ---- */
  $$(".tabs").forEach((tabs) => {
    const root = tabs.parentElement;
    $$(".tab", tabs).forEach((t) => {
      t.addEventListener("click", () => {
        $$(".tab", tabs).forEach((x) => x.setAttribute("aria-selected", String(x === t)));
        $$(".tabpanel", root).forEach((p) => p.classList.toggle("active", p.dataset.panel === t.dataset.tab));
      });
    });
  });

  /* ---- audio: um de cada vez ---- */
  let current = null;
  let currentBtn = null;
  $$(".voice-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      if (current && currentBtn === btn) {
        current.pause(); current = null; btn.setAttribute("aria-pressed", "false"); return;
      }
      if (current) { current.pause(); currentBtn.setAttribute("aria-pressed", "false"); }
      current = new Audio(btn.dataset.audio);
      currentBtn = btn;
      btn.setAttribute("aria-pressed", "true");
      current.addEventListener("ended", () => { btn.setAttribute("aria-pressed", "false"); current = null; });
      current.play().catch(() => { btn.setAttribute("aria-pressed", "false"); });
    });
  });

  /* ---- lightbox ---- */
  const lb = $("#lightbox");
  if (lb) {
    const img = $("img", lb);
    $$("[data-zoom]").forEach((b) => b.addEventListener("click", () => {
      const src = $("img", b);
      img.src = src.currentSrc || src.src;
      img.alt = src.alt;
      lb.classList.add("open");
    }));
    lb.addEventListener("click", () => lb.classList.remove("open"));
    document.addEventListener("keydown", (e) => { if (e.key === "Escape") lb.classList.remove("open"); });
  }

  /* ================= v4 ================= */
  const small = window.matchMedia("(max-width: 700px)");
  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)");

  /* ---- camada de cada secao no cabecalho dela ---- */
  $$("section[data-layer]").forEach((sec) => {
    const eb = $(".eyebrow", sec);
    if (eb) eb.dataset.layerLabel = sec.dataset.layer;
  });

  /* ---- medidor de profundidade: Y 64 no ceu ate Y -59 no bedrock ---- */
  const yEls = $$("[data-y]");
  const biomeEl = $("[data-biome]");
  const meter = $(".depth .m");
  const hero = $("#inicio");
  const layers = $$("section[data-layer]");
  let tick = false;
  function depth() {
    tick = false;
    const max = document.documentElement.scrollHeight - window.innerHeight;
    const p = max > 0 ? Math.min(1, Math.max(0, window.scrollY / max)) : 0;
    const y = Math.round(64 - p * 123);
    yEls.forEach((e) => { e.textContent = y; });
    if (meter) meter.style.setProperty("--p", (p * 100).toFixed(1) + "%");
    if (biomeEl) {
      const mid = window.scrollY + window.innerHeight * 0.5;
      let name = hero && mid < hero.offsetTop + hero.offsetHeight ? "CÉU" : "SUPERFÍCIE";
      layers.forEach((s) => { if (mid >= s.offsetTop) name = s.dataset.layer; });
      if (p > 0.985) name = "Bedrock";
      biomeEl.textContent = name.toUpperCase();
    }
  }
  window.addEventListener("scroll", () => { if (!tick) { tick = true; requestAnimationFrame(depth); } }, { passive: true });
  window.addEventListener("resize", depth);
  depth();

  /* ---- "Mais": folha com todas as secoes (celular) ---- */
  const sheet = $("#more-sheet");
  const grid = $("#sheet-grid");
  const moreBtn = $("#more-btn");
  if (sheet && grid && moreBtn) {
    $$(".hotbar li.sec a").forEach((a) => {
      const c = a.cloneNode(true);
      c.addEventListener("click", closeSheet);
      grid.appendChild(c);
    });
    function openSheet() { sheet.hidden = false; moreBtn.setAttribute("aria-expanded", "true"); const f = $("a", grid); if (f) f.focus(); }
    function closeSheet() { sheet.hidden = true; moreBtn.setAttribute("aria-expanded", "false"); moreBtn.focus(); }
    moreBtn.addEventListener("click", openSheet);
    $("#sheet-close").addEventListener("click", closeSheet);
    sheet.addEventListener("click", (e) => { if (e.target === sheet) closeSheet(); });
    document.addEventListener("keydown", (e) => { if (e.key === "Escape" && !sheet.hidden) closeSheet(); });
  }

  /* ---- conversa de exemplo digitando no topo ---- */
  const tYou = $("#t-you");
  const tMob = $("#t-mob");
  const tBox = $("#t-mob-box");
  if (tYou && tMob && tBox && !reduced.matches) {
    const demo = [
      ["Oi, tudo bem?", "Vaca", "Muuu! O capim hoje está uma delícia."],
      ["Quer trocar trigo?", "Aldeão", "Quero, três fenos por uma esmeralda."],
      ["Tem alguém aí?", "Zumbi", "Humano... sinto cheiro de carne."],
      ["Posso ir com você?", "Lobo", "Se me trouxer um osso, a gente conversa."],
      ["Fala baixo.", "Warden", "Cada passo seu treme o chão."]
    ];
    const wait = (ms) => new Promise((r) => setTimeout(r, ms));
    async function type(el, text, speed) {
      el.classList.add("typing");
      el.textContent = "";
      for (const ch of text) {
        el.textContent += ch;
        await wait(speed);
        while (document.hidden) await wait(400);
      }
      el.classList.remove("typing");
    }
    (async function loop() {
      let i = 0;
      await wait(2600);
      for (;;) {
        const [you, who, mob] = demo[i % demo.length];
        tBox.classList.add("idle");
        await type(tYou, you, 55);
        await wait(450);
        tBox.dataset.who = who;
        tBox.classList.remove("idle");
        await type(tMob, mob, 32);
        await wait(3200);
        i++;
      }
    })();
  }

  /* ---- instalacao no celular: so o passo atual aberto ---- */
  function nextOpen() {
    const first = steps.find((s) => !s.classList.contains("done"));
    steps.forEach((s) => s.classList.toggle("closed", s !== first));
    return first;
  }
  function fold() {
    steps.forEach((s) => {
      const h = $(".gui > h3", s);
      s.classList.toggle("fold", small.matches);
      if (!h) return;
      if (small.matches) { h.setAttribute("role", "button"); h.tabIndex = 0; }
      else { h.removeAttribute("role"); h.removeAttribute("tabindex"); h.removeAttribute("aria-expanded"); }
    });
    if (small.matches) { nextOpen(); syncAria(); } else steps.forEach((s) => s.classList.remove("closed"));
  }
  function syncAria() {
    steps.forEach((s) => { const h = $(".gui > h3", s); if (h) h.setAttribute("aria-expanded", String(!s.classList.contains("closed"))); });
  }
  steps.forEach((s) => {
    const h = $(".gui > h3", s);
    const cb = $("input[type=checkbox]", s);
    if (h) {
      const toggle = () => { if (!small.matches) return; s.classList.toggle("closed"); syncAria(); };
      h.addEventListener("click", toggle);
      h.addEventListener("keydown", (e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); toggle(); } });
    }
    if (cb) cb.addEventListener("change", () => {
      if (!small.matches || !cb.checked) return;
      const nxt = nextOpen();
      syncAria();
      if (nxt) nxt.scrollIntoView({ behavior: reduced.matches ? "auto" : "smooth", block: "start" });
    });
  });
  small.addEventListener("change", fold);
  fold();

  /* ---- funcoes no celular: toque no cartao para ler tudo ---- */
  $$(".feat").forEach((f) => f.addEventListener("click", (e) => {
    if (!small.matches || e.target.closest("a")) return;
    f.classList.toggle("open");
  }));
})();
