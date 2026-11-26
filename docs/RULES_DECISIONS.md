# Keputusan Aturan (PRD §18) & Asumsi Implementasi

Semua keputusan berikut diimplementasikan di `engine/GameEngine.kt` dan diuji di `EngineTest.kt`.
Parameter yang bisa disetel saat playtest ditandai **[tunable]**.

## Klarifikasi PRD §18

| § | Pertanyaan | Keputusan |
|---|---|---|
| 18.1 | PAIR/TRIPLE/QUAD: warna sama? | Angka sama saja, warna bebas. Legal bila ≥1 kartu cocok warna aktif atau angka kartu teratas. Kartu terakhir yang dipilih menjadi kartu teratas dan menentukan warna aktif baru. |
| 18.2 | Plus Chain wajib cocok warna? | Tidak. Hanya jenis kartu yang menentukan: PAIR +1, PLUS_TWO +2, TRIPLE +3, QUAD +4. Setelah set angka, warna aktif = warna kartu terakhir; PLUS_TWO tidak mengubah warna. |
| 18.3 | Durasi MONOPOLY | Berakhir saat giliran pemilik MONOPOLY dimulai lagi. Selama kunci hanya kartu angka (tunggal/kombo) dengan kartu teratas berwarna terkunci yang legal; kartu aksi & WILD tidak. Tidak bisa mencocokkan → otomatis ambil 2 (ambil sukarela tetap 1). |
| 18.4 | STRAIGHT | ≥3 angka berurutan, warna bebas, 9→0 **tidak** berurutan. Kartu otomatis diurut naik; kartu tertinggi jadi teratas dan menentukan warna aktif. Arah permainan selalu dibalik. |
| 18.5 | DEBT/SKIP/REVERSE | Setiap pemain punya penghitung `skipTurns`; SKIP dan DEBT menambahnya, dikonsumsi saat giliran pemain itu akan dimulai. REVERSE hanya membalik arah dan tidak memengaruhi penghitung. DEBT: target ambil 2 (bisa direspons) lalu kehilangan giliran berikutnya. |
| 18.6 | Kartu terakhir + efek tertunda | Efek kartu diselesaikan dulu; menang berlaku segera setelah BACKSTAB! tervalidasi, penalti tertunda dibuang. Gagal mengumumkan → ambil 2 dan permainan berlanjut. |
| 18.7 | Draw Pile habis | Discard Pile selain kartu teratas dikocok menjadi Draw Pile baru. |
| 18.8 | Prioritas respons | Tidak ada prioritas level. Satu rantai hanya boleh satu respons pertahanan (IMMUNITY / DEFLECT / BOOMERANG). Setelah DEFLECT, target baru tidak bisa bertahan lagi tetapi boleh melanjutkan rantai atau mengambil. BOOMERANG ilegal bila penyerang awal adalah dirinya/telah tereliminasi. |

## Asumsi tambahan (PRD tidak spesifik)

- **Kartu aksi tidak berwarna** (sesuai `card_manifest.json`): selalu boleh dimainkan pada giliran sendiri, kecuali kartu pertahanan yang hanya sebagai respons.
- Respons pertahanan berlaku untuk penalti: rantai Plus, DEBT, CHAOS. TAX, PICKPOCKET, EXCHANGE, HIJACK, MUTINY tidak bisa dipatahkan.
- Mengambil kartu (sukarela) → bila kartu tarikan legal, boleh langsung dimainkan atau LEWATI.
- Mengambil penalti mengakhiri giliran (PRD §8.4).
- STAB!/BACKSTAB!: jendela pengumuman **3,6 detik** [tunable] (`CALL_WINDOW_MS`); bot selalu mengumumkan.
- HIJACK: kartu diberikan dipilih pemain **setelah** mengambil 2 kartu. MUTINY: kartu yang diberikan dipilih acak.
- Pemain tereliminasi: kartunya masuk ke bawah Discard Pile (Card Conservation tetap 120).
- Dua pemain: REVERSE/STRAIGHT membalik arah tetapi lawan tetap pemain berikutnya.

## Penyimpangan yang disengaja: Tekanan Last Standing **[tunable]**

Dengan aturan murni PRD, bot-vs-bot di Last Standing hampir tidak pernah mencapai 15 kartu karena pemain terus
membuang kartu. Simulasi 1.500 game (seed tetap):

| `lsAnte` (kartu tambahan per kartu dimainkan) | Game macet (>6000 langkah) | Median langkah (5 pemain) |
|---|---|---|
| 0 (PRD murni) | 704 / 1500 | 4.174 |
| 1 | 337 / 1500 | 4.174 |
| **2 (dipakai)** | **0 / 1500** | **143** |

Pada mode Last Standing, setiap kali pemain memainkan kartu/kombo ia menarik `lsAnte` (=2) kartu. Ubah di
`GameState.lsAnte` bila playtest menunjukkan terlalu cepat/lambat; set 0 untuk aturan PRD murni.
Mode First to Zero tidak terpengaruh (median 43–63 langkah, semua selesai).
