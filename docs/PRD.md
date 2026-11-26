# BACKSTAB!
## PRODUCT REQUIREMENTS DOCUMENT (PRD)

**Version:** 2.3 — Final  
**Product Type:** Digital Competitive Card Game  
**Initial Platform:** Android  
**Future Platform:** Physical Card Game  
**Genre:** Competitive Card Game, Hand Management, Take-That, Combo Chaining, Social Manipulation, Satire  
**Target Audience:** 13+  
**Players:** 2–6 players  
**Recommended Players:** 4–5 players  
**Estimated Match Duration:** 15–30 minutes  

**Tagline:**  
*Trust Nobody. Play Dirty. Win Anyway.*

---

# 1. PRODUCT OVERVIEW

## 1.1 Product Description

BACKSTAB! adalah permainan kartu kompetitif yang menggabungkan strategi, manipulasi, pengambilan keputusan, serangan, pertahanan, dan interaksi antarpemain.

Pemain harus mengelola kartu di tangan, memanfaatkan efek kartu, membentuk kombinasi, serta membaca strategi lawan untuk mencapai kemenangan.

Permainan dirancang dengan pendekatan kompetitif dan satir. Pemain dapat menyerang, mengalihkan hukuman, mencuri kartu, bertukar tangan, membentuk rantai penalti, hingga memengaruhi arah permainan.

Tidak ada pemain yang benar-benar aman. Keunggulan dapat berubah sewaktu-waktu karena satu kartu atau satu keputusan.

## 1.2 Product Vision

Menciptakan permainan kartu digital yang mudah dipahami, tetapi memiliki kedalaman strategi melalui kombinasi kartu, efek berantai, dan interaksi antarpemain.

BACKSTAB! harus memberikan pengalaman permainan yang:
- Mudah dipelajari oleh pemain baru.
- Memiliki ruang strategi yang cukup untuk pemain berpengalaman.
- Menghadirkan interaksi kompetitif secara langsung.
- Menghasilkan situasi permainan yang tidak mudah diprediksi.
- Memiliki identitas visual yang kuat dan konsisten.
- Dapat dikembangkan menjadi permainan kartu fisik tanpa mengubah mekanik intinya.

## 1.3 Core Experience

Pengalaman utama BACKSTAB! berpusat pada tiga hal:

1. **Strategic Card Management** — mengatur kartu dan menentukan waktu terbaik untuk menggunakannya.
2. **Player Manipulation** — memengaruhi kondisi pemain lain melalui serangan, pencurian, pertukaran, dan pengalihan penalti.
3. **Risk and Reward** — mengambil risiko untuk memperoleh keuntungan atau menghindari konsekuensi yang lebih besar.

## 1.4 Product Principles

- Setiap kartu harus memiliki fungsi yang jelas.
- Efek kartu harus dapat dipahami dan diprediksi sebelum dimainkan.
- Interaksi antarpemain harus menjadi bagian utama permainan.
- Tidak ada mekanik yang membuat pemain kehilangan kendali tanpa kejelasan aturan.
- Informasi penting seperti jumlah kartu, arah permainan, warna aktif, dan penalti harus selalu mudah dibaca.
- Visual boleh memiliki kedalaman 3D, tetapi aset utama tetap menggunakan ilustrasi dan elemen 2D.

---

# 2. TARGET PLATFORM & RELEASE PLAN

## 2.1 Initial Release

**Platform:** Android

Versi awal difokuskan pada implementasi seluruh mekanik inti dan pengujian keseimbangan permainan.

Prioritas pengembangan:
- Implementasi sistem permainan berbasis giliran.
- Implementasi seluruh jenis kartu.
- Implementasi kombinasi kartu.
- Implementasi Plus Chain.
- Implementasi dua mode permainan.
- Implementasi sistem validasi aturan.
- Implementasi antarmuka permainan.
- Implementasi presentasi visual 2.5D.
- Pengujian keseimbangan melalui playtest.

## 2.2 Development Roadmap

| Tahap | Fokus |
|---|---|
| Phase 1 — Android MVP | Implementasi mekanik dan sistem permainan inti |
| Phase 2 — Playtesting | Pengujian aturan, keseimbangan kartu, dan durasi permainan |
| Phase 3 — Iteration | Penyesuaian berdasarkan hasil pengujian |
| Phase 4 — Physical Edition | Adaptasi kartu digital menjadi Print & Play atau produksi fisik |

Pengembangan versi fisik harus mempertahankan jumlah kartu, efek, kombinasi, dan aturan inti yang sama dengan versi digital.

---

# 3. GAME MODES

BACKSTAB! memiliki dua mode permainan utama.

## 3.1 First to Zero

**Objective:** Menjadi pemain pertama yang berhasil menghabiskan seluruh kartu di tangan.

Pemain harus mengurangi jumlah kartu hingga tersisa satu, kemudian menghabiskan kartu terakhir untuk memenangkan permainan.

### STAB!

Ketika pemain hanya memiliki satu kartu tersisa, pemain wajib mengumumkan:

**STAB!**

Pengumuman ini menandakan bahwa pemain berada pada kondisi satu kartu terakhir.

### BACKSTAB!

Ketika pemain berhasil memainkan kartu terakhir dan tidak memiliki kartu tersisa, pemain wajib mengumumkan:

**BACKSTAB!**

Pemain tersebut dinyatakan sebagai pemenang setelah kondisi kemenangan divalidasi.

### Failure to Call

Jika pemain gagal mengumumkan STAB! atau BACKSTAB! dan kegagalannya diketahui sebelum giliran berikutnya atau sebelum validasi kemenangan, pemain tersebut menerima penalti:

- Mengambil 2 kartu dari Draw Pile.
- Permainan dilanjutkan.

Validasi pengumuman harus menjadi bagian dari pemeriksaan kondisi permainan.

## 3.2 Last Standing

**Objective:** Menjadi pemain terakhir yang masih bertahan.

Dalam mode ini, pemain tidak memenangkan permainan dengan menghabiskan kartu.

Sebaliknya, pemain harus menghindari kondisi eliminasi.

### Elimination Rule

Pemain akan tereliminasi ketika jumlah kartu di tangannya mencapai atau melebihi 15 kartu.

**Elimination Threshold:** 15 cards.

Nilai ini dapat disesuaikan selama pengujian keseimbangan pada versi Android, tetapi nilai awal yang digunakan adalah 15.

### Victory Condition

- Pemain yang mencapai batas eliminasi dikeluarkan dari permainan.
- Permainan berlanjut selama masih terdapat lebih dari satu pemain yang belum tereliminasi.
- Pemain terakhir yang tersisa menjadi pemenang.

---

# 4. DECK COMPOSITION

## 4.1 Total Cards

Jumlah kartu dalam satu deck adalah tepat:

**120 CARDS**

Komposisi deck dibagi menjadi:

| Category | Quantity |
|---|---:|
| Number Cards | 80 |
| Action, Chaos, and Penalty Cards | 40 |
| **Total** | **120** |

## 4.2 Number Cards

Terdapat empat warna utama:

- Crimson
- Azure
- Emerald
- Amber

Setiap warna memiliki angka 0–9 dengan dua salinan untuk setiap angka.

Perhitungan:

4 warna × 10 angka × 2 salinan = 80 kartu.

| Color | Number Range | Copies per Number | Total |
|---|---|---:|---:|
| Crimson | 0–9 | 2 | 20 |
| Azure | 0–9 | 2 | 20 |
| Emerald | 0–9 | 2 | 20 |
| Amber | 0–9 | 2 | 20 |
| **Total** | | | **80** |

## 4.3 Action, Chaos, and Penalty Cards

| Card | Quantity | Primary Function |
|---|---:|---|
| DEFLECT | 4 | Mengalihkan seluruh penalti kepada pemain berikutnya |
| BOOMERANG | 3 | Mengembalikan serangan kepada penyerang awal |
| IMMUNITY | 3 | Membatalkan serangan atau rantai penalti |
| PICKPOCKET | 4 | Mengambil satu kartu acak dari pemain berikutnya |
| EXCHANGE | 3 | Bertukar seluruh kartu dengan pemain pilihan |
| HIJACK | 3 | Mengambil dua kartu acak dan memberikan satu kartu |
| MONOPOLY | 2 | Mengunci warna aktif dan memberikan penalti kepada pemain yang tidak dapat mencocokkan |
| TAX | 2 | Membuat seluruh pemain lain mengambil satu kartu |
| DEBT | 2 | Membuat pemain pilihan mengambil dua kartu dan kehilangan giliran berikutnya |
| REVERSE | 2 | Membalik arah permainan |
| SKIP | 2 | Melewati giliran pemain berikutnya |
| WILD | 2 | Memilih warna aktif secara bebas |
| CHAOS | 4 | Membuat pemain berikutnya mengambil tiga kartu dan mengubah arah permainan |
| MUTINY | 2 | Memindahkan satu kartu dari setiap pemain kepada tetangga sesuai arah permainan |
| PLUS_TWO | 2 | Memulai atau melanjutkan rantai penalti +2 |
| **Total** | **40** | |

Tidak terdapat kartu RESET dalam deck.

---

# 5. CARD POWER SYSTEM

Kartu dikelompokkan ke dalam lima level berdasarkan kategori kekuatan dan kompleksitas efek.

Level menunjukkan klasifikasi kartu, bukan aturan otomatis bahwa kartu dengan level lebih tinggi dapat membatalkan semua kartu dengan level lebih rendah.

## Level 1 — Basic Cards

Berisi kartu angka 0–9.

Fungsi:
- Menjadi kartu dasar untuk mencocokkan warna atau angka.
- Digunakan dalam kombinasi kartu.
- Menjadi dasar pengelolaan jumlah kartu di tangan.

Urutan nilai angka:

9 > 8 > 7 > 6 > 5 > 4 > 3 > 2 > 1 > 0.

## Level 2 — Basic Combos & Turn Control

Berisi:
- PAIR
- PLUS_TWO
- TRIPLE
- QUAD
- STRAIGHT
- TAX
- DEBT
- SKIP
- REVERSE

Kartu dan kombinasi ini memberikan keuntungan melalui penalti, perubahan arah, pengaturan giliran, atau perubahan kondisi pemain.

## Level 3 — Advanced Manipulation

Berisi:
- DEFLECT
- PICKPOCKET
- EXCHANGE
- HIJACK

Fokus utama level ini adalah memanipulasi kartu dan kondisi pemain lain.

## Level 4 — Defensive & Counter Cards

Berisi:
- IMMUNITY
- BOOMERANG

Kartu ini digunakan untuk menghadapi serangan dan efek penalti.

Kartu pertahanan tidak dapat dialihkan menggunakan DEFLECT.

## Level 5 — High Impact Cards

Berisi:
- CHAOS
- MONOPOLY
- MUTINY
- WILD

Kartu level ini memiliki pengaruh besar terhadap kondisi permainan, arah permainan, warna aktif, atau distribusi kartu.

**Catatan:** Tingkat kekuatan tidak secara otomatis menentukan urutan penyelesaian efek. Resolusi setiap kartu tetap mengikuti aturan efek dan aturan respons yang berlaku.

---

# 6. CARD EFFECT SPECIFICATIONS

## 6.1 DEFLECT

**Quantity:** 4  
**Category:** Defensive / Redirection  
**Power Level:** 3

Mengalihkan seluruh penalti yang sedang diterima pemain kepada pemain berikutnya berdasarkan arah permainan saat ini.

Rules:
- Seluruh total penalti dialihkan.
- Pemain yang menggunakan DEFLECT tidak mengambil penalti tersebut.
- Hanya satu kali pengalihan DEFLECT yang diperbolehkan dalam satu rangkaian penalti.
- DEFLECT tidak dapat digunakan untuk mengalihkan kartu pertahanan.
- Pengalihan mengikuti arah permainan yang berlaku saat efek diproses.

## 6.2 BOOMERANG

**Quantity:** 3  
**Category:** Counterattack  
**Power Level:** 4

Mengembalikan serangan atau penalti kepada pemain yang pertama kali memulai serangan tersebut.

Rules:
- Penalti dikembalikan kepada original attacker.
- BOOMERANG tidak dapat dialihkan kembali menggunakan DEFLECT.
- Target pengembalian ditentukan berdasarkan identitas pemain yang memulai serangan awal.

## 6.3 IMMUNITY

**Quantity:** 3  
**Category:** Defense  
**Power Level:** 4

Membatalkan serangan atau seluruh rantai penalti yang sedang berlangsung.

Rules:
- Rantai penalti dibatalkan sepenuhnya.
- Pemain yang menggunakan IMMUNITY tidak mengambil kartu dari rantai tersebut.
- Efek serangan yang menjadi bagian dari rantai yang dibatalkan tidak diteruskan.

## 6.4 PICKPOCKET

**Quantity:** 4  
**Category:** Manipulation  
**Power Level:** 3

Pemain berikutnya harus memberikan satu kartu acak dari tangannya kepada pemain yang menggunakan PICKPOCKET.

Rules:
- Kartu target dipilih secara acak.
- Target tidak memilih kartu yang diberikan.
- Kartu yang berpindah ditambahkan ke tangan pemain yang menggunakan PICKPOCKET.

## 6.5 EXCHANGE

**Quantity:** 3  
**Category:** Manipulation  
**Power Level:** 3

Pemain yang menggunakan EXCHANGE memilih satu pemain lain untuk bertukar seluruh kartu di tangan.

Rules:
- Seluruh kartu kedua pemain dipertukarkan.
- Jumlah kartu masing-masing pemain berubah mengikuti jumlah kartu yang diterima.
- Efek hanya melibatkan kedua pemain tersebut.

## 6.6 HIJACK

**Quantity:** 3  
**Category:** Manipulation  
**Power Level:** 3

Pemain memilih satu target, kemudian mengambil dua kartu acak dari tangan target dan memberikan satu kartu dari tangannya sendiri kepada target yang sama.

Rules:
- Dua kartu target diambil secara acak.
- Pemain memberikan satu kartu dari tangannya kepada target.
- Kartu yang diberikan dipilih oleh pemain yang menggunakan HIJACK.
- Semua perpindahan kartu harus diselesaikan sebagai bagian dari satu resolusi efek.

## 6.7 MONOPOLY

**Quantity:** 2  
**Category:** Color Control  
**Power Level:** 5

Pemain menentukan satu warna yang menjadi warna aktif dan mengunci warna tersebut selama satu putaran penuh.

Rules:
- Warna yang dipilih menjadi warna aktif.
- Pemain lain yang tidak dapat mencocokkan warna tersebut harus mengambil dua kartu.
- Pemain yang menggunakan MONOPOLY tidak terkena efek penalti dari kartunya sendiri.
- Durasi satu putaran penuh harus mengikuti definisi durasi MONOPOLY yang ditetapkan dalam aturan final.

## 6.8 TAX

**Quantity:** 2  
**Category:** Global Penalty  
**Power Level:** 2

Seluruh pemain selain pengguna TAX harus mengambil satu kartu.

Rules:
- Semua pemain lain mengambil satu kartu.
- Pemain yang menggunakan TAX tidak mengambil kartu.
- Efek berlaku kepada seluruh pemain lain yang masih berada dalam permainan.

## 6.9 DEBT

**Quantity:** 2  
**Category:** Targeted Penalty  
**Power Level:** 2

Pemain memilih satu target untuk menerima penalti.

Rules:
- Target mengambil dua kartu.
- Target kehilangan giliran berikutnya.
- Efek kehilangan giliran harus dicatat agar tidak terlewat ketika urutan giliran berjalan.

## 6.10 REVERSE

**Quantity:** 2  
**Category:** Direction Control  
**Power Level:** 2

Membalik arah permainan.

Rules:
- Arah searah jarum jam berubah menjadi berlawanan arah jarum jam.
- Arah berlawanan arah jarum jam berubah menjadi searah jarum jam.
- Giliran selanjutnya mengikuti arah baru.

## 6.11 SKIP

**Quantity:** 2  
**Category:** Turn Control  
**Power Level:** 2

Melewati giliran pemain berikutnya.

Rules:
- Pemain yang seharusnya mendapat giliran dilewati.
- Permainan berpindah kepada pemain berikutnya sesuai arah yang berlaku.

## 6.12 WILD

**Quantity:** 2  
**Category:** Color Control  
**Power Level:** 5

Pemain bebas memilih warna aktif berikutnya.

Rules:
- Tidak memerlukan kecocokan warna sebelumnya.
- Warna aktif berubah sesuai pilihan pemain.
- Giliran selanjutnya menggunakan warna baru tersebut.

## 6.13 CHAOS

**Quantity:** 4  
**Category:** Global Direction & Penalty  
**Power Level:** 5

Memberikan penalti kepada pemain berikutnya dan mengubah arah permainan.

Rules:
- Pemain berikutnya mengambil tiga kartu.
- Arah permainan berubah.
- Arah baru berlaku untuk giliran selanjutnya.

## 6.14 MUTINY

**Quantity:** 2  
**Category:** Global Card Transfer  
**Power Level:** 5

Setiap pemain memberikan satu kartu tertutup kepada tetangganya sesuai arah permainan saat ini.

Rules:
- Setiap pemain memberikan satu kartu.
- Perpindahan mengikuti arah permainan yang sedang berlaku.
- Kartu diberikan dalam keadaan tertutup.
- Perpindahan dilakukan sebagai satu efek bersama.

## 6.15 PLUS_TWO

**Quantity:** 2  
**Category:** Penalty Chain  
**Power Level:** 2

Memulai atau melanjutkan rantai penalti dengan nilai +2.

Rules:
- Penalti bertambah dua.
- Pemain berikutnya dapat merespons menggunakan kartu penalti yang memenuhi aturan Plus Chain.
- Jika rantai tidak dapat dilanjutkan, pemain target mengambil seluruh total penalti.

---

# 7. COMBO SYSTEM

Combo memungkinkan pemain memainkan beberapa kartu sebagai satu aksi.

## 7.1 PAIR

**Requirement:** Dua kartu dengan angka yang sama.

Effect:
- Memulai penalti +1.
- Pemain berikutnya menerima kesempatan untuk melanjutkan rantai penalti sesuai aturan Plus Chain.

## 7.2 TRIPLE

**Requirement:** Tiga kartu dengan angka yang sama.

Effect:
- Memulai penalti +3.
- Pemain berikutnya menerima kesempatan untuk melanjutkan rantai penalti.

## 7.3 QUAD

**Requirement:** Empat kartu dengan angka yang sama.

Effect:
- Memulai penalti +4.
- Pemain berikutnya menerima kesempatan untuk melanjutkan rantai penalti.

## 7.4 STRAIGHT

**Requirement:** Tiga atau lebih kartu dengan angka berurutan.

Effect:
- Mengubah arah permainan.
- Mengubah warna aktif sesuai hasil kombinasi.

STRAIGHT harus memenuhi aturan urutan angka dan pemilihan warna yang berlaku.

---

# 8. PLUS CHAIN SYSTEM

## 8.1 Overview

Plus Chain adalah sistem penalti berantai yang memungkinkan pemain meneruskan atau meningkatkan total penalti kepada pemain berikutnya.

Sistem ini menjadi salah satu mekanik strategis utama BACKSTAB!.

## 8.2 Starting a Chain

Plus Chain dapat dimulai menggunakan:
- PAIR: +1
- PLUS_TWO: +2
- TRIPLE: +3
- QUAD: +4

Setelah rantai dimulai, pemain target menerima kesempatan untuk melanjutkan penalti menggunakan kartu atau kombinasi penalti yang memenuhi ketentuan.

## 8.3 Continuing a Chain

Jika pemain memiliki kartu penalti yang memenuhi aturan:
- Pemain dapat memainkan kartu tersebut.
- Nilai penalti ditambahkan ke total sebelumnya.
- Target penalti berpindah kepada pemain berikutnya.

Contoh:

| Action | Total Penalty |
|---|---:|
| Player A plays PAIR | +1 |
| Player B plays PLUS_TWO | +3 |
| Player C plays TRIPLE | +6 |
| Player D plays QUAD | +10 |

Total penalti akan terus bertambah selama rantai dapat dilanjutkan.

## 8.4 Breaking a Chain

Jika pemain tidak dapat atau tidak ingin melanjutkan rantai:
- Pemain mengambil seluruh kartu sesuai total penalti.
- Rantai berakhir.
- Giliran pemain tersebut selesai.

## 8.5 Defensive Responses

Terdapat tiga aturan respons utama:

**IMMUNITY**
- Membatalkan seluruh rantai penalti.
- Total penalti tidak perlu diambil oleh pemain yang menggunakan IMMUNITY.

**DEFLECT**
- Mengalihkan seluruh total penalti kepada pemain berikutnya.
- Hanya satu pengalihan DEFLECT yang diperbolehkan dalam satu rantai.

**BOOMERANG**
- Mengembalikan seluruh serangan atau penalti kepada original attacker.
- Penalti tidak dapat dialihkan kembali menggunakan DEFLECT.

## 8.6 Defense Restriction

- Kartu pertahanan tidak dapat dialihkan menggunakan DEFLECT.
- Satu rantai penalti tidak boleh menggabungkan beberapa jenis respons pertahanan dalam satu penyelesaian.
- Resolusi pertahanan harus mengikuti aturan respons yang berlaku, bukan sekadar membandingkan level kartu.

---

# 9. GAME SETUP

## 9.1 Initial Setup

1. Seluruh 120 kartu dikocok.
2. Setiap pemain menerima tujuh kartu.
3. Sisa kartu ditempatkan sebagai Draw Pile.
4. Satu kartu dibuka untuk menjadi Discard Pile.
5. Kartu awal harus berupa Number Card.
6. Jika kartu awal yang terbuka merupakan Action, Chaos, atau Penalty Card, kartu tersebut dikembalikan dan dikocok ulang sebelum pengambilan kartu awal diulangi.
7. Arah permainan awal adalah searah jarum jam.

## 9.2 Starting Player

Permainan dimulai oleh pemain pertama sesuai urutan awal yang ditentukan oleh sistem permainan.

## 9.3 Initial Game State

Ketika permainan dimulai:
- Setiap pemain memiliki tujuh kartu.
- Draw Pile berisi seluruh kartu yang belum dibagikan atau dibuka.
- Discard Pile berisi satu Number Card.
- Warna aktif mengikuti warna kartu awal.
- Arah permainan adalah searah jarum jam.
- Tidak ada penalti aktif.
- Tidak ada pemain yang tereliminasi.

---

# 10. CORE GAMEPLAY LOOP

## 10.1 Player Turn

Pada setiap giliran, pemain dapat melakukan salah satu tindakan yang legal:

1. Memainkan satu kartu yang sesuai.
2. Memainkan kombinasi kartu yang memenuhi syarat.
3. Memainkan kartu khusus yang tidak memerlukan kecocokan biasa.
4. Mengambil satu kartu dari Draw Pile.

## 10.2 Card Matching

Kartu dapat dimainkan jika memenuhi ketentuan kecocokan yang berlaku, yaitu:
- Warna sama dengan warna aktif.
- Angka sama dengan kartu terakhir yang dimainkan.
- Simbol atau jenis kartu memenuhi aturan kecocokan.
- Kartu memiliki efek khusus yang memperbolehkan permainan tanpa kecocokan biasa.

## 10.3 Drawing a Card

Jika pemain tidak memainkan kartu:
- Pemain mengambil satu kartu dari Draw Pile.
- Jika kartu yang diambil dapat dimainkan secara legal, pemain boleh langsung memainkannya.
- Jika kartu tidak dapat dimainkan atau pemain memilih tidak memainkannya, giliran berakhir.

## 10.4 Playing a Card

Setelah kartu dimainkan:
- Kartu dipindahkan ke Discard Pile.
- Efek kartu diperiksa.
- Efek kartu diselesaikan.
- Kondisi permainan diperbarui.
- Sistem memeriksa kondisi kemenangan atau eliminasi.
- Giliran berikutnya ditentukan berdasarkan arah dan efek permainan.

## 10.5 Turn Resolution

Setiap aksi harus melalui validasi sebelum diterapkan pada Game State.

Sistem tidak boleh mengubah jumlah kartu, giliran, atau kondisi permainan berdasarkan aksi yang tidak valid.

---

# 11. VICTORY & ELIMINATION VALIDATION

## 11.1 First to Zero Validation

Sistem memeriksa:
- Jumlah kartu pemain setelah kartu dimainkan.
- Apakah pemain telah mengumumkan STAB! ketika memiliki satu kartu.
- Apakah pemain telah mengumumkan BACKSTAB! ketika menghabiskan kartu terakhir.
- Apakah ada penalti akibat kegagalan pengumuman.

Pemain hanya dinyatakan menang jika kondisi kemenangan telah divalidasi.

## 11.2 Last Standing Validation

Setelah setiap efek yang mengubah jumlah kartu:
- Sistem memeriksa jumlah kartu setiap pemain.
- Pemain dengan 15 kartu atau lebih dinyatakan tereliminasi.
- Pemain tereliminasi tidak lagi menerima giliran.
- Sistem memeriksa apakah hanya satu pemain yang tersisa.

---

# 12. VISUAL & ART DIRECTION

## 12.1 Core Visual Concept

**2D Assets with 3D Presentation (2.5D)**

BACKSTAB! menggunakan aset grafis 2D, tetapi ditampilkan dengan pendekatan visual yang memberikan kesan berada dalam lingkungan permainan 3D.

Tujuan utamanya adalah menciptakan pengalaman seperti permainan kartu digital yang berlangsung di atas meja tiga dimensi.

Penting untuk membedakan antara bentuk aset dan cara aset ditampilkan:

- Kartu tetap berupa aset 2D.
- Ilustrasi kartu tetap 2D.
- Ikon dan simbol tetap 2D.
- UI tetap 2D.
- Kedalaman visual dibuat melalui perspektif, pencahayaan, bayangan, lapisan, dan transformasi.

Tidak diperlukan perubahan seluruh aset menjadi model 3D.

## 12.2 Camera & Perspective

**Camera Type: Top-Down Table Perspective with Slight Perspective**

Kamera menggunakan sudut pandang dari atas meja dengan sedikit perspektif, mengikuti referensi visual permainan domino yang diberikan.

Karakteristik kamera:
- Seluruh area meja terlihat dari atas.
- Meja ditampilkan dalam orientasi horizontal.
- Permukaan meja terlihat sedikit memiliki kedalaman dan perspektif.
- Posisi pemain dapat terlihat mengelilingi meja.
- Area kartu pemain sendiri terlihat pada bagian bawah layar.
- Area permainan utama tetap terlihat jelas di tengah meja.
- Kamera bersifat statis selama permainan normal.

Kamera tidak menggunakan sudut pandang first-person atau perspektif seolah-olah pemain sedang memegang kartu.

Sudut pandang ini harus memberikan visibilitas yang baik terhadap kartu, posisi pemain, dan keseluruhan kondisi permainan.

## 12.3 Card Design

Setiap kartu menggunakan ilustrasi dan desain permukaan 2D.

Kesan kedalaman dapat dibangun melalui:
- Rounded corners.
- Bevel atau frame depth.
- Sisi kartu yang terlihat tipis.
- Contact shadow.
- Ambient shadow.
- Soft directional lighting.
- Highlight pada bagian permukaan.
- Perspektif dan rotasi kartu.

Kartu harus tetap mudah dibaca ketika berada dalam kondisi normal, terpilih, dimainkan, atau berada dalam tumpukan.

## 12.4 Table Environment

Lingkungan permainan menggunakan meja bergaya stylized.

Karakteristik:
- Permukaan meja memiliki perspektif.
- Draw Pile dan Discard Pile terlihat memiliki volume melalui tumpukan kartu.
- Pencahayaan memberikan kesan ruang dan kedalaman.
- Dekorasi lingkungan tidak boleh mengganggu keterbacaan kartu.
- Area permainan tetap menjadi fokus visual utama.

## 12.5 Lighting & Shadow

Pencahayaan digunakan untuk membangun kesan tiga dimensi tanpa mengubah aset menjadi model 3D.

Penggunaan:
- Directional lighting.
- Ambient lighting.
- Soft shadow.
- Contact shadow.
- Highlight pada permukaan kartu.
- Bayangan antar-lapisan kartu.

Efek pencahayaan harus konsisten di seluruh elemen permainan.

## 12.6 Animation Direction

Animasi menggunakan transformasi dan perspektif terhadap aset 2D.

Animasi yang diperlukan untuk presentasi mekanik yang sudah ada:

| Interaction | Animation Direction |
|---|---|
| Select Card | Kartu sedikit terangkat dari susunan tangan |
| Card Focus | Highlight pada kartu terpilih |
| Play Card | Kartu bergerak menuju area permainan dengan perspektif |
| Draw Card | Kartu bergerak dari Draw Pile menuju tangan pemain |
| Discard | Kartu ditempatkan pada tumpukan Discard Pile |
| PICKPOCKET | Kartu berpindah dari pemain target |
| EXCHANGE | Seluruh susunan kartu kedua pemain bertukar |
| HIJACK | Kartu berpindah sesuai efek pencurian dan pemberian |
| PLUS CHAIN | Nilai penalti diperbarui secara visual |
| REVERSE | Indikator arah permainan berubah |
| CHAOS | Perubahan arah dan penalti ditampilkan secara visual |
| MUTINY | Kartu berpindah antar-pemain sesuai arah permainan |

Animasi harus memberikan umpan balik yang jelas terhadap aksi pemain, tetapi tidak boleh memperlambat alur permainan secara berlebihan.

## 12.7 Color Palette

Palet warna utama:

| Color | Hex | Usage |
|---|---|---|
| Crimson | #B91C1C | Attack / Penalty |
| Azure | #1D4ED8 | Control / Manipulation |
| Emerald | #047857 | Defense |
| Amber | #B45309 | Economy / Monopoly |
| Black | #111827 | Base / Dark Elements |
| Gold | #F59E0B | Accent / Supreme Chaos |

Warna harus memiliki kontras yang cukup untuk membedakan jenis kartu dan efeknya.

## 12.8 Visual Identity Summary

| Element | Visual Direction |
|---|---|
| Card Artwork | 2D |
| Card Frame | 2D with visual depth |
| Card Rendering | 2.5D |
| Table Environment | 2.5D / 3D-style |
| Camera | Top-Down Table Perspective with Slight Perspective |
| Lighting | Directional & Ambient |
| Shadow | Layered Soft Shadow |
| Animation | 2D Transform & Perspective |
| UI | 2D with visual depth |
| Art Direction | Dark Stylized |

**Core Visual Statement:**

BACKSTAB! harus terlihat seperti permainan kartu digital yang berlangsung di atas meja tiga dimensi, tetapi ilustrasi, kartu, ikon, dan UI tetap menggunakan aset 2D. Kesan tiga dimensi berasal dari cara aset tersebut ditampilkan, bukan dari mengubah seluruh aset menjadi model 3D.

---

# 13. USER INTERFACE REQUIREMENTS

## 13.1 Main Game Interface

Antarmuka permainan harus memprioritaskan informasi yang dibutuhkan pemain selama pertandingan.

Elemen utama:
- Kartu di tangan pemain.
- Kartu terakhir pada Discard Pile.
- Draw Pile.
- Informasi pemain lain.
- Jumlah kartu setiap pemain.
- Warna aktif.
- Arah permainan.
- Informasi penalti aktif.
- Indikator pemain yang sedang mendapat giliran.

## 13.2 Card Interaction

Pemain harus dapat:
- Memilih kartu.
- Memainkan kartu.
- Memilih beberapa kartu untuk kombinasi.
- Melihat efek kartu sebelum mengonfirmasi aksi yang relevan.
- Melihat kartu yang dapat dimainkan.
- Membedakan kartu yang tidak legal untuk dimainkan.

## 13.3 Game State Information

Informasi yang memengaruhi keputusan pemain harus ditampilkan dengan jelas.

Termasuk:
- Pemain aktif.
- Arah permainan.
- Warna aktif.
- Total penalti.
- Target penalti.
- Status Plus Chain.
- Status STAB!.
- Status BACKSTAB!.
- Status eliminasi.

## 13.4 Visual Feedback

Setiap perubahan kondisi permainan harus memiliki umpan balik visual yang sesuai.

Contoh:
- Warna aktif berubah ketika WILD digunakan.
- Arah permainan berubah ketika REVERSE digunakan.
- Penalti bertambah ketika Plus Chain dilanjutkan.
- Kartu berpindah ketika efek manipulasi dijalankan.
- Status pemain diperbarui ketika pemain tereliminasi.

---

# 14. TECHNICAL REQUIREMENTS

## 14.1 Game Architecture

Sistem permainan harus menggunakan struktur berbasis state agar seluruh aturan dapat diproses secara konsisten.

### Game States

| State | Description |
|---|---|
| Setup | Menyiapkan permainan dan membagikan kartu |
| InitialCard | Menentukan kartu awal |
| PlayerTurn | Menunggu tindakan pemain |
| ValidateAction | Memvalidasi tindakan pemain |
| PlayCard | Memproses kartu yang dimainkan |
| Combo | Memvalidasi dan memproses kombinasi |
| ResolveEffect | Menyelesaikan efek kartu |
| PenaltyChain | Memproses rantai penalti |
| DefenseResponse | Memproses respons pertahanan |
| PenaltyDraw | Memproses pengambilan kartu penalti |
| ValidateCall | Memvalidasi STAB! dan BACKSTAB! |
| VictoryCheck | Memeriksa kemenangan atau eliminasi |

## 14.2 Card Data Structure

Setiap kartu harus memiliki data yang dapat dibaca oleh sistem permainan.

Struktur dasar:

| Field | Description |
|---|---|
| card_id | Identitas unik kartu |
| card_type | Jenis kartu |
| name | Nama kartu |
| color | Warna kartu |
| value | Nilai angka jika tersedia |
| power_level | Level kekuatan |
| effect_type | Kategori efek |
| penalty_value | Nilai penalti jika tersedia |

Data kartu harus dipisahkan dari logika efek agar sistem dapat memproses setiap jenis kartu secara konsisten.

## 14.3 Game State Data

Game State harus menyimpan informasi berikut:

| Data | Description |
|---|---|
| players | Daftar pemain |
| hands | Kartu yang dimiliki setiap pemain |
| draw_pile | Kartu yang tersedia untuk diambil |
| discard_pile | Kartu yang telah dimainkan |
| current_player | Pemain yang sedang mendapat giliran |
| direction | Arah permainan |
| active_color | Warna aktif |
| penalty_total | Total penalti Plus Chain |
| penalty_target | Target penalti saat ini |
| original_attacker | Pemain yang memulai serangan |
| deflect_used | Status penggunaan DEFLECT dalam rantai |
| skipped_turns | Status giliran yang harus dilewati |
| stab_status | Status pengumuman STAB! |
| backstab_status | Status pengumuman BACKSTAB! |
| eliminated_players | Daftar pemain yang tereliminasi |
| game_mode | Mode permainan aktif |

## 14.4 Core Validation

Semua tindakan harus divalidasi oleh sistem sebelum perubahan Game State diterapkan.

Validasi mencakup:
- Kecocokan warna dan angka.
- Legalitas kartu.
- Persyaratan kombinasi.
- Penggunaan efek kartu.
- Total penalti.
- Target penalti.
- Respons pertahanan.
- Urutan giliran.
- Arah permainan.
- Kondisi kemenangan.
- Kondisi eliminasi.

## 14.5 Card Conservation Invariant

Jumlah kartu harus selalu konsisten.

Rumus:

**Total Cards = Cards in Players' Hands + Draw Pile + Discard Pile**

Nilai total harus tetap 120.

Jika implementasi menggunakan zona sementara untuk kartu yang sedang diproses, kartu pada zona tersebut juga harus dihitung agar tidak terjadi kehilangan atau duplikasi kartu.

---

# 15. GAMEPLAY RULE CONSISTENCY

Untuk menghindari perbedaan hasil antara tampilan dan sistem, setiap aksi harus diproses melalui urutan berikut:

1. Pemain memilih aksi.
2. Sistem memeriksa legalitas aksi.
3. Jika aksi tidak legal, Game State tidak berubah.
4. Jika aksi legal, sistem memproses perpindahan kartu.
5. Sistem menjalankan efek kartu atau kombinasi.
6. Sistem memperbarui jumlah kartu, arah, warna aktif, dan status penalti.
7. Sistem memeriksa kondisi STAB!, BACKSTAB!, atau eliminasi.
8. Sistem menentukan pemain berikutnya.
9. Giliran berikutnya dimulai.

Urutan tersebut menjadi acuan implementasi agar tidak terjadi konflik antar-efek.

---

# 16. PLAYTEST & BALANCING

Pengujian dilakukan untuk memastikan permainan tetap kompetitif, adil, dan memiliki durasi yang sesuai.

## 16.1 Primary Metrics

Data yang perlu dikumpulkan:

| Metric | Purpose |
|---|---|
| Match Duration | Mengukur durasi rata-rata pertandingan |
| Average Hand Size | Mengukur perubahan jumlah kartu |
| Draw Pile Remaining | Mengukur konsumsi kartu |
| Chain Frequency | Mengukur seberapa sering Plus Chain terjadi |
| Chain Break Rate | Mengukur seberapa sering pemain gagal melanjutkan penalti |
| Defense Usage | Mengukur penggunaan kartu pertahanan |
| Card Effect Frequency | Mengukur frekuensi penggunaan setiap efek |
| Win Distribution | Mengukur persebaran kemenangan pemain |

## 16.2 Balancing Objectives

Pengujian bertujuan untuk mengetahui:
- Apakah satu jenis kartu terlalu dominan.
- Apakah efek tertentu terlalu jarang atau terlalu sering digunakan.
- Apakah Plus Chain terlalu mudah berkembang.
- Apakah pemain memiliki kesempatan yang cukup untuk bertahan.
- Apakah mode First to Zero dan Last Standing memiliki durasi yang sesuai.
- Apakah jumlah kartu dan distribusinya menghasilkan pengalaman kompetitif yang baik.

Perubahan keseimbangan tidak boleh dilakukan tanpa mempertimbangkan dampaknya terhadap jumlah deck dan konsistensi aturan.

---

# 17. SCOPE & EXCLUSIONS

## 17.1 Included in Scope

- Dua mode permainan utama.
- Deck berjumlah tepat 120 kartu.
- Seluruh kartu angka.
- Seluruh kartu Action, Chaos, dan Penalty.
- Sistem Power Level.
- Sistem Combo.
- Sistem Plus Chain.
- Sistem pertahanan.
- Sistem giliran.
- Sistem validasi kemenangan.
- Sistem eliminasi.
- Antarmuka permainan.
- Presentasi visual 2.5D menggunakan aset 2D.
- Pengujian dan penyeimbangan mekanik.

## 17.2 Excluded from Scope

Dokumen ini tidak menambahkan:
- Jenis kartu baru.
- Mode permainan tambahan.
- Mekanik permainan baru.
- Sistem progresi di luar mekanik inti.
- Sistem ekonomi tambahan.
- Fitur sosial tambahan.
- Perubahan jumlah kartu.
- Perubahan tujuan utama kedua mode permainan.

Pengembangan difokuskan pada penyempurnaan dan implementasi sistem yang sudah ditentukan dalam dokumen ini.

---

# 18. RULE CLARIFICATIONS FOR FINAL IMPLEMENTATION

Bagian ini mencatat aturan yang belum memiliki definisi cukup spesifik pada rancangan awal. Bagian ini bukan fitur tambahan, melainkan keputusan aturan yang perlu ditetapkan agar implementasi tidak menghasilkan interpretasi berbeda.

## 18.1 Combo Color Requirement

Untuk PAIR, TRIPLE, dan QUAD:

Apakah semua kartu harus memiliki angka sama sekaligus warna sama, atau cukup memiliki angka yang sama meskipun warnanya berbeda?

## 18.2 Plus Chain Matching

Ketika pemain melanjutkan Plus Chain:

Apakah kartu penalti harus tetap cocok dengan warna aktif, atau selama jenis kartu dan nilai penalti memenuhi syarat, kartu dapat dimainkan tanpa kecocokan warna?

## 18.3 MONOPOLY Duration

Definisi satu putaran penuh:

Apakah efek berakhir tepat ketika pemain pengguna MONOPOLY mendapatkan giliran berikutnya, atau setelah seluruh pemain lain mendapatkan satu kesempatan bermain?

## 18.4 STRAIGHT Rules

Ketentuan STRAIGHT perlu menetapkan:
- Apakah kartu dalam kombinasi boleh memiliki warna berbeda?
- Apakah angka 9 dan 0 dianggap berurutan?
- Bagaimana warna aktif ditentukan setelah STRAIGHT dimainkan?
- Apakah arah perubahan ditentukan oleh urutan angka atau aturan khusus?

## 18.5 DEBT Turn Interaction

Jika target DEBT menerima efek kehilangan giliran, bagaimana efek tersebut berinteraksi dengan SKIP, REVERSE, atau efek lain yang mengubah urutan giliran?

## 18.6 First to Zero with Pending Effects

Jika pemain menghabiskan kartu terakhir ketika masih terdapat efek atau penalti yang belum selesai:

Apakah kemenangan langsung berlaku, atau efek yang masih tertunda harus diselesaikan terlebih dahulu?

## 18.7 Empty Draw Pile

Jika Draw Pile habis ketika pemain diwajibkan mengambil kartu:

Bagaimana sistem mengisi ulang Draw Pile dan bagaimana Discard Pile diperlakukan?

## 18.8 Power Level and Response Priority

Power Level hanya menunjukkan kategori kekuatan kartu dan bukan prioritas otomatis.

Urutan respons harus ditetapkan secara eksplisit, khususnya ketika pemain memiliki lebih dari satu kemungkinan respons pertahanan.

---

# 19. FINAL PRODUCT DEFINITION

BACKSTAB! adalah permainan kartu kompetitif untuk 2–6 pemain dengan durasi pertandingan sekitar 15–30 menit.

Permainan menggunakan deck tetap berjumlah 120 kartu, terdiri dari 80 Number Cards dan 40 Action, Chaos, serta Penalty Cards.

Pemain bersaing melalui pengelolaan kartu, kombinasi, serangan, manipulasi, pertahanan, dan rantai penalti.

Terdapat dua mode utama:
- **First to Zero:** pemain pertama yang menghabiskan seluruh kartu menjadi pemenang.
- **Last Standing:** pemain terakhir yang tidak tereliminasi menjadi pemenang.

Versi awal dikembangkan untuk Android, kemudian diuji dan disempurnakan sebelum diadaptasi menjadi permainan kartu fisik.

Dari sisi visual, BACKSTAB! menggunakan pendekatan **2D Assets with 3D Presentation (2.5D)**. Seluruh aset utama tetap berupa grafis 2D, sementara perspektif, pencahayaan, bayangan, lapisan, dan animasi menciptakan kesan permainan berlangsung di atas meja tiga dimensi.

Kamera menggunakan sudut pandang dari atas meja dengan sedikit perspektif, sehingga keseluruhan area permainan dan posisi pemain dapat terlihat seperti referensi visual yang diberikan.

## FINAL DESIGN PRINCIPLE

**BACKSTAB! is a game of strategy, manipulation, and betrayal.**

**Trust Nobody. Play Dirty. Win Anyway.**