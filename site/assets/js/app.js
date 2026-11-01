/* MobVoz IA: interacoes do site (sem dependencias). Tudo que usa localStorage e opcional e protegido. */
(function () {
  "use strict";

  const $ = (s, r) => (r || document).querySelector(s);
  const $$ = (s, r) => Array.from((r || document).querySelectorAll(s));
  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)");
  document.documentElement.classList.add("js");

  const store = {
    get(k) { try { return window.localStorage.getItem(k); } catch (e) { return null; } },
    set(k, v) { try { window.localStorage.setItem(k, v); } catch (e) { /* sem armazenamento: segue sem salvar */ } }
  };

  /* ---- aparecer ao rolar ---- */
  const reveals = $$(".reveal");
  if ("IntersectionObserver" in window && !reduced.matches) {
    const io = new IntersectionObserver((entries) => entries.forEach((e) => {
      if (e.isIntersecting) { e.target.classList.add("in"); io.unobserve(e.target); }
    }), { rootMargin: "0px 0px -8% 0px" });
    reveals.forEach((el) => io.observe(el));
  } else {
    reveals.forEach((el) => el.classList.add("in"));
  }

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
        btn.textContent = "Ctrl+C";
      }
      setTimeout(() => { btn.textContent = "Copiar"; }, 1800);
    });
  });

  /* ---- abas (API / local) ---- */
  $$(".tabs").forEach((tabs) => {
    const root = tabs.parentElement;
    $$(".tab", tabs).forEach((t) => t.addEventListener("click", () => {
      $$(".tab", tabs).forEach((x) => x.setAttribute("aria-selected", String(x === t)));
      $$(".tabpanel", root).forEach((p) => p.classList.toggle("active", p.dataset.panel === t.dataset.tab));
    }));
  });

  /* ---- vozes: um audio de cada vez ---- */
  let current = null;
  let currentBtn = null;
  $$(".voice-btn").forEach((btn) => btn.addEventListener("click", () => {
    if (current && currentBtn === btn) { current.pause(); current = null; btn.setAttribute("aria-pressed", "false"); return; }
    if (current) { current.pause(); currentBtn.setAttribute("aria-pressed", "false"); }
    current = new Audio(btn.dataset.audio);
    currentBtn = btn;
    btn.setAttribute("aria-pressed", "true");
    current.addEventListener("ended", () => { btn.setAttribute("aria-pressed", "false"); current = null; });
    current.play().catch(() => btn.setAttribute("aria-pressed", "false"));
  }));

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

  /* ---- galeria: setas ---- */
  const gal = $("#gallery");
  if (gal) {
    const step = () => Math.max(260, Math.round(gal.clientWidth * 0.7));
    const go = (d) => gal.scrollBy({ left: d * step(), behavior: reduced.matches ? "auto" : "smooth" });
    $("#gal-prev").addEventListener("click", () => go(-1));
    $("#gal-next").addEventListener("click", () => go(1));
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
    const type = async (el, text, speed) => {
      el.classList.add("typing");
      el.textContent = "";
      for (const ch of text) {
        el.textContent += ch;
        await wait(speed);
        while (document.hidden) await wait(400);
      }
      el.classList.remove("typing");
    };
    (async function loop() {
      let i = 0;
      await wait(2200);
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

  /* ---- funcionalidades: busca + categorias ---- */
  const cats = $$(".cat");
  if (cats.length) {
    const search = $("#q");
    const pills = $$(".pill");
    const empty = $("#empty");
    let cat = "todas";
    const norm = (s) => s.toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "");
    const index = $$(".feat").map((el) => ({ el, text: norm(el.textContent), cat: el.closest(".cat").dataset.cat }));
    function filter() {
      const q = norm(search.value.trim());
      let shown = 0;
      index.forEach((f) => {
        const ok = (cat === "todas" || f.cat === cat) && (!q || f.text.includes(q));
        f.el.hidden = !ok;
        if (ok) shown++;
      });
      cats.forEach((c) => { c.hidden = !$$(".feat:not([hidden])", c).length; });
      empty.style.display = shown ? "none" : "block";
    }
    pills.forEach((p) => p.addEventListener("click", () => {
      cat = p.dataset.cat;
      pills.forEach((x) => x.setAttribute("aria-pressed", String(x === p)));
      filter();
      if (cat !== "todas") window.scrollTo({ top: 0, behavior: reduced.matches ? "auto" : "smooth" });
    }));
    search.addEventListener("input", filter);
    if (location.hash) {
      const h = pills.find((p) => "#" + p.dataset.cat === location.hash);
      if (h) h.click();
    }
  }

  /* ---- instalacao: checklist salvo no navegador ---- */
  const steps = $$("#steps .step");
  if (steps.length) {
    const bar = $("#prog-bar");
    const txt = $("#prog-txt");
    const KEY = "mobvoz.passos";
    let saved = [];
    try { saved = JSON.parse(store.get(KEY) || "[]"); } catch (e) { saved = []; }
    const update = (announce) => {
      const done = steps.filter((s) => s.classList.contains("done")).length;
      bar.style.width = (done / steps.length * 100) + "%";
      txt.textContent = done + " de " + steps.length + " passos";
      store.set(KEY, JSON.stringify(steps.map((s, i) => (s.classList.contains("done") ? i + 1 : 0)).filter(Boolean)));
      if (!announce) return;
      if (done === steps.length) conquista("Instalou o MobVoz! Agora é só conversar com os mobs.");
      else if (done === 1) conquista("Primeiro passo concluído");
      else if (done === Math.ceil(steps.length / 2)) conquista("Metade do caminho");
    };
    steps.forEach((s, i) => {
      const cb = $("input[type=checkbox]", s);
      if (!cb) return;
      if (saved.includes(i + 1)) { cb.checked = true; s.classList.add("done"); }
      cb.addEventListener("change", () => {
        s.classList.toggle("done", cb.checked);
        update(cb.checked);
        const next = steps.find((x) => !x.classList.contains("done"));
        if (cb.checked && next) next.scrollIntoView({ behavior: reduced.matches ? "auto" : "smooth", block: "start" });
      });
    });
    update(false);
  }
})();
