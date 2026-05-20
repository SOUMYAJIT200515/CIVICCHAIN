import os
import re

INPUT_FILE = "frontend/login.html"
OUTPUT_DIR = "frontend/outputs"
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "login.html")


# ── READ FILE ───────────────────────────────────────────────
with open(INPUT_FILE, "r", encoding="utf-8") as f:
    content = f.read()


# ── 1) ADD BIOMETRIC CSS ────────────────────────────────────
bio_css = """
/* ── WEBAUTHN BIOMETRIC ── */
.bio-divider{display:flex;align-items:center;gap:10px;margin:20px 0;font-family:'JetBrains Mono',monospace;font-size:.6rem;color:var(--muted);letter-spacing:.12em;}
.bio-divider::before,.bio-divider::after{content:'';flex:1;height:1px;background:var(--border);}
.bio-btn{width:100%;padding:13px;background:transparent;border:1px solid rgba(0,229,255,.25);color:var(--accent);font-family:'JetBrains Mono',monospace;font-size:.75rem;letter-spacing:.08em;cursor:pointer;display:flex;align-items:center;justify-content:center;gap:10px;transition:all .25s;border-radius:4px;position:relative;overflow:hidden;margin-bottom:6px;}
.bio-btn:hover{border-color:var(--accent);background:rgba(0,229,255,.05);box-shadow:0 0 20px rgba(0,229,255,.1);}
.bio-btn:disabled{opacity:.35;cursor:not-allowed;}
.bio-icon{font-size:1.2rem;}
.bio-status{display:none;align-items:center;gap:8px;padding:9px 13px;font-family:'JetBrains Mono',monospace;font-size:.65rem;border-radius:4px;margin-top:8px;}
.bio-status.show{display:flex;}
.bio-status.s{background:rgba(16,185,129,.07);border:1px solid rgba(16,185,129,.2);color:var(--success);}
.bio-status.e{background:rgba(239,68,68,.07);border:1px solid rgba(239,68,68,.2);color:var(--error);}
.bio-status.i{background:rgba(0,229,255,.06);border:1px solid rgba(0,229,255,.18);color:var(--accent);}
.bio-scanning{display:none;text-align:center;margin:10px 0;}
.bio-scanning.show{display:block;}
"""

content = content.replace("</style>", bio_css + "\n</style>", 1)


# ── 2) UPDATE STEP INDICATOR ────────────────────────────────
old_steps = """<div class="steps-indicator">"""
new_steps = """
<div class="steps-indicator">
  <div class="step-ind active" id="si1"><div class="step-circle">1</div><span>IDENTITY</span></div>
  <div class="step-line" id="sl12"></div>
  <div class="step-ind" id="si2"><div class="step-circle">2</div><span>OTP</span></div>
  <div class="step-line" id="sl23"></div>
  <div class="step-ind" id="si3"><div class="step-circle">3</div><span>BIOMETRIC</span></div>
  <div class="step-line" id="sl34"></div>
  <div class="step-ind" id="si4"><div class="step-circle">4</div><span>WALLET</span></div>
"""
content = content.replace(old_steps, new_steps, 1)


# ── 3) ADD BIOMETRIC UI IN STEP 1 ───────────────────────────
insert_point = '<div class="form-step active" id="step-1">'
bio_block = """
<div class="bio-divider">QUICK LOGIN</div>
<button class="bio-btn" onclick="alert('Biometric coming soon')">
  <span class="bio-icon">🖐️</span> Login with Fingerprint
</button>
"""

content = content.replace(insert_point, insert_point + bio_block, 1)


# ── 4) MODIFY STEP 3 → BIOMETRIC + STEP 4 WALLET ────────────
content = content.replace("STEP 3: Wallet", "STEP 3: Biometric")

wallet_block = """
<!-- STEP 4: Wallet -->
<div class="form-step" id="step-4">
  <h3>Connect Wallet</h3>
  <button onclick="connectWallet()">Connect MetaMask</button>
  <button onclick="goStep(3)">Back</button>
</div>
"""

content = content.replace("</body>", wallet_block + "\n</body>")


# ── 5) SIMPLE SCRIPT EXTENSION ──────────────────────────────
extra_js = """
<script>
function connectWallet(){
  alert("Wallet connected (demo)");
}
</script>
"""

content = content.replace("</script>", extra_js + "\n</script>", 1)


# ── WRITE OUTPUT ────────────────────────────────────────────
os.makedirs(OUTPUT_DIR, exist_ok=True)

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write(content)

print("✅ Patch complete!")
print("📄 Output:", OUTPUT_FILE)