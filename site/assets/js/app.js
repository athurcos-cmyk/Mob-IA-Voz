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
})();
