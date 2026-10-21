drop database if exists Pharmatex;
create database Pharmatex;
use Pharmatex;

CREATE TABLE Cliente(
                        email varchar(50) primary key,
                        passwordEmail varchar(60) not null,
                        nome varchar(30) not null,
                        cognome varchar(30) not null,
                        dataDiNascita char(10) not null,
                        numeroTelefono char(10) not null,
                        codiceFiscale char(16) not null,
                        via varchar(300) not null,
                        citta varchar(50) not null,
                        cap char(5) not null,
                        provincia char(2) not null,
                        nazione char(20) not null,
                        amministratore boolean
);

CREATE TABLE Prodotto(
                         idProdotto char(5) primary key,
                         nomeCategoria varchar(30),
                         nomeProd varchar(50) not null,
                         descrizione text not null,
                         larghezza double,
                         lunghezza double,
                         prezzo double not null,
                         quantita int not null,
                         tipoMaterialeMaterasso varchar(30),
                         coloreLetto varchar(30),
                         materialeRete varchar(30),
                         rivestimentoDivano varchar(30),
                         coloreDivano varchar(30),
                         tipoStoffaCuscino varchar(30),
                         materialeCuscino varchar(30),
                         formaCuscino varchar(30)
);

CREATE TABLE Acquistare(
                           emailCliente char(50) references Cliente(email),
                           idProdotto char(5) references Prodotto(idProdotto),
                           quantitaAcquistata int
);


CREATE TABLE CartaDiCredito(
                               numeroCarta varchar(16) primary key,
                               nomeIntestatario varchar(30) not null,
                               dataScadenza char(10) not null,
                               CVV char(3) not null,
                               emailProprietario char(50) references Cliente(email)
);

-- NOTA: Gli hash BCrypt sottostanti corrispondono alle password:
-- admin001, Cliente1, Cliente2
INSERT INTO Cliente VALUES
                        ("admin@gmail.com",'$2a$12$6vZ1eZ1eZ1eZ1eZ1eZ1eZu5Hq3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3Z3',"Nome","Cognome","2021-07-01","1234567890","ABCDE25F67G160H","Indirizzo, civico","Città","12345","NA","Italia",1),
                        ("cliente1@gmail.com",'$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',"Nome","Cognome","2021-07-01","1234567890","ABCDE25F67G160H","Indirizzo, civico","Città","12345","AV","Italia",0),
                        ("cliente2@gmail.com",'$2a$12$5Z2vQ5jz5F5v5Z2vQ5jz5u5Z2vQ5jz5F5v5Z2vQ5jz5F5v5Z2vQ5jz5',"Nome","Cognome","2021-07-01","1234567890","ABCDE25F67G160H","Indirizzo, civico","Città","12345","AV","Italia",0);

insert INTO Prodotto values
                         ("MM001","MaterassoMatrimoniale","Nuvola","1800 molle insacchettate e 7 cm di memory, da 30 cm. Completamente sfoderabile com doppia fascia 3D made in italy e doppia cerniera.Le molle insacchettate mantengono la schiena dritta e il memory da la giusta morbidezza per un giusto sostegno",180,190,400,5,"Memory",null,null,null,null,null,null,null),
                         ("MM002","MaterassoMatrimoniale","3000 micromolle","3000 micromolle e memory, da 25 cm, completamente sfoderabile con doppia fascia e doppia cerniera",180,190,400,5,"Molla",null,null,null,null,null,null,null),
                         ("MM003","MaterassoMatrimoniale","Roma","1500 molle insacchettate e memory da 25 cm, non sfoderabile in tessuto aloe vera con doppia fascia traspirante",180,190,239,5,"Memory",null,null,null,null,null,null,null),
                         ("MM004","MaterassoMatrimoniale","Elegance","1200 molle insacchettate e memory, da 25 cm completamente sfoderabile con doppia fascia e doppia cerniera",180,190,400,5,"Memory",null,null,null,null,null,null,null),
                         ("MM005","MaterassoMatrimoniale","Giglio","Memory foam da 25 cm, completamente sfoderabile con doppia fascia e doppia cerniera. Non adatto ad un peso superiore agli 80 kili",180,190,350,5,"Memory",null,null,null,null,null,null,null),
                         ("MS006","MaterassoSingolo","Bonnel","800 molle in acciaio bonnel da 25 cm non sfoderabile",180,190,160,5,"Lattice",null,null,null,null,null,null,null),
                         ("MM007","MaterassoMatrimoniale","Titanio","900 Molle in titanio da 25 cm completamente sfoderabile, adatte ad un peso superiore ai 90 kili",180,190,400,5,"Molle",null,null,null,null,null,null,null),
                         ("MS008","MaterassoSingolo","30centimetri","1400 molle insacchettate e memory da 30 cm non sfoderabile con doppia fascia traspirante",80,190,200,5,"Memory",null,null,null,null,null,null,null),
                         ("MS009","MaterassoSingolo","Zeus","1500 molle insacchettate e memory da 25 cm non sfoderabile in tessuto aloe vera con doppia fascia traspirante",80,190,239,5,"Memory",null,null,null,null,null,null,null),
                         ("MS010","MaterassoSingolo","Titanio","900 Molle in titanio da 25 cm completamente sfoderabile, adatte ad un peso superiore ai 90 kili",80,190,400,5,"Molle",null,null,null,null,null,null,null),
                         ("MPM11","MaterassoPiazzaEMezzo","Elisir","Materasso in schiuma dalle proprietà igieniche e imbottitura in memory",120,190,340,5,"Lana",null,null,null,null,null,null,null),
                         ("MPM12","MaterassoPiazzaEMezzo","Aiir San","Materasso in memory con Aiir Technology e proprietà igieniche",120,190,475.00,5,"Memory",null,null,null,null,null,null,null),
                         ("MPM13","MaterassoPiazzaEMezzo","Clima Memory 4.0","Materasso in memory con rivestimento Outlast® per il benessere termico.",120,190,735.00,5,"Memory",null,null,null,null,null,null,null),
                         ("LCM14","LettoContenitoreMatrimoniale","Dublino","Un letto con una testata particolare, due grandi cuscini accoglienti per una piacevole lettura serale, è facile da aprire e comodo da rifare",210,230,300,5,null,"Blu",null,null,null,null,null,null),
                         ("LCM15","LettoContenitoreMatrimoniale","Casablanca","Un letto con una testata particolare, due grandi cuscini accoglienti per una piacevole lettura serale, è facile da aprire e comodo da rifare.",210,230,350,5,null,"Beige",null,null,null,null,null,null),
                         ("LCP16","LettoContenitorePiazzaEMezzo","Firenze","Costruito con parti in tubolare e parti forgiate a mano, questo letto va montato con rete gambe a terra.",150,210,410,5,null,"Grigio",null,null,null,null,null,null),
                         ("LCP17","LettoContenitorePiazzaEMezzo","Panama","Il letto Panama è 100% Made in Italy certificato e con la struttura testiera sobria e svasata su cui si inseriscono in un perfetto incastro ampi e soffici cuscinoni su cui rilassarsi",150,210,899.25,5,null,"Marrone",null,null,null,null,null,null),
                         ("LCS18","LettoContenitoreSingolo","Amsterdam","Dritti all'essenziale con il nostro letto di Helsinki! Dai alla cameretta dei tuoi piccoli il tocco di originalità che manca con questo elegantissimo modello.",110,210,240,5,null,"Grigio",null,null,null,null,null,null),
                         ("LCS19","LettoContenitoreSingolo","Parigi","Modello affascinante, fiore all'occhiello della nostra produzione. La struttura è in legno massello di abete e multitrato di pioppo, la rete a doghe ha un robusto telaio in legno.",110,210,210.49,5,null,"Grigio chiaro",null,null,null,null,null,null),
                         ("MPM20","MaterassoPiazzaEMezzo","Easy Comfort Plus 4.0","Materasso Made in Italy in schiuma Waterfoam ergonomica. Imbottitura in fibra ad alta resistenza ai batteri. Fascia laterale in tessuto Air Space blu per la massima traspirabilità. Sfoderabile su 3 lati e lavabile.",190,120,149.00,5,"Lattice",null,null,null,null,null,null,null),
                         ("MPM21","MaterassoPiazzaEMezzo","Grey Mind","Materasso Made in Italy in schiuma Waterfoam. Due gradi di rigidità grazie all’imbottitura superiore in memory Visco Mind ergonomico e automodellante e inferiore in fibra dalle proprietà anallergiche. Rivestimento in tessuto Silver&Life dalle antimicrobiche e antistatiche. Sfoderabile su 3 lati e lavabile",190,140,262.00,5,"Lattice",null,null,null,null,null,null,null),
                         ("MPM22","MaterassoPiazzaEMezzo","Materasso Elisir", "Materasso Made in Italy in schiuma Waterfoam. Portanza migliorata grazie ai tre diversi strati di schiuma Waterfoam e all’imbottitura in memory Visco Mind. Fascia laterale in Air Space che favorisce il dissipamento dell’umidità. Rivestimento in tessuto Soft Care dalla straordinaria morbidezza, sfoderabile su 4 lati e lavabile",195,140,437.00,5,"Memory",null,null,null,null,null,null,null),
                         ("MPM23","MaterassoPiazzaEMezzo","Materasso Adam 2.0", "Materasso Made in Italy in schiuma Waterfoam. Due gradi di rigidità grazie all’imbottitura superiore in memory Visco Mind e inferiore in schiuma Extra Soft Foam. Fascia laterale in Air Space che favorisce il dissipamento dell’umidità. Rivestimento in fibre dalle proprietà anallergiche, sfoderabile su 4 lati e lavabile.",200,140,579.00,5,"Lana",null,null,null,null,null,null,null),
                         ("MS024","MaterassoSingolo","Materasso Giglio","Materasso Made in Italy con lastra in 100% schiuma di lattice strutturata a 7 zone di portanza differenziata. Imbottitura in fibre dalle proprietà anallergiche. Rivestimento in maglia Stretch, un materiale che, con la sua composizione in 100% fibra con proprietà anallergiche, garantisce un ottimo livello di igiene e comfort; sfoderabile su 3 lati e lavabile.", 200, 90, 313.00,5,"Lattice",null,null,null,null,null,null,null),
                         ("MS025","MaterassoSingolo","Materasso Bel Riposo Fresh 3.1", "Materasso Made in Italy con lastra in memory Visco Mind:registered: e schiuma Waterfoam. Imbottitura in Visco Mind:registered: Climafresh:registered: che, grazie alla presenza di speciali microcapsule, mantiene costante la temperatura del prodotto, assorbendo il calore corporeo. Due differenti gradi di rigidità. Rivestimento sfoderabile su 4 lati e lavabile.", 190, 85, 446.00, 5, "Memory", null,null,null,null,null,null,null),
                         ("RE026","ReteSingolaElettrica","Faggio","Rete elettrica di faggio con possibilità di alzata piedi e testa",80,190,570.99,5,null,null,"Faggio",null,null,null,null,null),
                         ("RE027","ReteMatrimonialeElettrica","Doghe","Rete elettrica di faggio con possibilità di alzata piedi e testa",180,190,587.33,5,null,null,"Faggio",null,null,null,null,null),
                         ("RE028","ReteMatrimonialeElettrica","Tecna","Rete elettrica in ferro con 14 doghe in faggio e regolatori di rigidità",180,190,530.30,5,null,null,"Ferro",null,null,null,null,null),
                         ("RE029","RetePiazzaEMezzoDoghe","Gold Soft","Rete a 26 doghe in faggio",140,195,329.00,5,null,null,"Faggio",null,null,null,null,null),
                         ("RD030","RetePiazzaEMezzoDoghe","Platino","Rete a 22 doghe separate in faggio, regolatori di rigidità, ammortizzatori e struttura in legno",140,200,479.00,5,null,null,"Faggio",null,null,null,null,null),
                         ("DL031","DivanoLetto","Divano Jack-Divano Letto","Divano letto 2 posti con materasso in schiuma",140,140,599.99,5,null,null,null,"Primo nero","Topo 205",null,null,null),
                         ("DL032","DivanoLetto","Divano Clark-Divano Letto","Divano letto con design moderno con materasso in memory alto 13 cm e piedini in alluminio nero alti 10 cm.",140,140,1049.00,5,null,null,null,"Roma","24 azzurro",null,null,null),
                         ("C0033","CuscinoViaggio","Salute e benessere","Guanciale con molecola a memoria, migliora la qualità del sonno, si adatta perfettamente alle forme del corpo, garantendo un corretto allineamento del collo e del rachide cervicale",70,45,40,5,null,null,null,null,null,"Poliestere","Memory","Rettangolare"),
                         ("C0034","CuscinoRelax","Greta memory foam","L'effetto confortante del materiale viscoelastico, biocompatibile in quanto assolutamente atossico e quindi può essere messo a diretto contatto con la pelle",70,45,40,5,null,null,null,null,null,"Cotone","Memory","Rettangolare"),
                         ("C0035","CuscinoGuanciale","Guanciale Softgel Moore","Tecnologie e materiali che favoriscono la corretta termoregolazione del calore corporeo per tutta la durata del sonno",70,45,30,5,null,null,null,null,null,"Poliestere","Memory","Rettangolare"),
                         ("P0036","DivanoPoltrona","Poltrona Cristina","Poltrona reclinabile a 2 motori",104,90,799.00,5,null,null,null,"Daytona","3 Panna",null,null,null),
                         ("LS037","LettoSingolo","Maverik Outlet","Letto singolo senza contenitore con cuscino applicato alla testata. Giroletto con struttura in legno di abete e testata in multistrato",85,220,299.99,2,null,"28 Ocra",null,null,null,null,null,null),
                         ("LS038","LettoSingolo","Charlie","Letto singolo o piazza e mezza con testata imbottita e cuciture verticali. Giroletto con struttura in legno di abete e testata in multistrato",85,195,379.00,3,null,"Viola ombretto",null,null,null,null,null,null),
                         ("LS039","LettoSingolo","Saturno","Struttura letto singola con secondo letto estraibile e sponde angolari. Rete a doghe inclusa nel prezzo. Compatibile con materassi di misura 80x190 di altezza massima 15 cm, non inclusi nel prezzo. Materasso e biancheria non inclusi nel prezzo.",80,190,529.00,5,null,"Blu elettrico",null,null,null,null,null,null),
                         ("LM040","LettoMatrimoniale","Istanbul","Struttura letto matrimoniale imbottita salvaspazio. Testata imbottita con lavorazione capitonné. Sollevamento del contenitore a compasso di serie; optional nella versione messa in piano.Materasso e biancheria non inclusi nel prezzo.",180,232,700,5,null,"Beige",null,null,null,null,null,null),
                         ("LM041","LettoMatrimoniale","Paddy"," Grande testata arricchita di impunture a voler richiamare l’estetica e il comfort dei materassi. Rete a doghe inclusa nel prezzo. Sollevamento del contenitore a compasso di serie; optional nella versione messa in piano. Materasso e biancheria non inclusi nel prezzo.",180,232,500,5,null,"blu",null,null,null,null,null,null),
                         ("LM042","LettoMatrimoniale","Aurora"," Testata imbottita rettangolare sdoppiata semplice e raffinata al tempo stesso, per un tocco di discreta originalità. Rete a doghe inclusa nel prezzo. Sollevamento del contenitore a compasso di serie; optional nella versione messa in piano. Materasso e biancheria non inclusi nel prezzo.",180,232,600,5,null,"verde",null,null,null,null,null,null),
                         ("DCP43","DivanoConPenisola","KIVIK","Scopri la morbidezza e la comodità del divano KIVIK. Grazie all’ampiezza, ai braccioli bassi e alle molle insacchettate con strato di schiuma che si adatta ai contorni del tuo corpo, KIVIK è l’ideale per rilassarsi o trascorrere del tempo con gli amici.",83,297,1099.00,5,null,null,null,"Madrid","Verde acqua",null,null,null),
                         ("DCP44","DivanoConPenisola","EKTORP","I divani della serie EKTORP hanno cuscini spessi e confortevoli e un design intramontabile. Grazie al prezzo accessibile, puoi acquistare una o più fodere extra e alternarle in base alla stagione o alla tua voglia di cambiare.",88,243,1099.00,5,null,null,null,"Hallarp grigio","Grigio",null,null,null),
                         ("DCP45","DivanoConPenisola","VIMLE","La serie VIMLE ha sezioni che puoi combinare come vuoi per creare una soluzione su misura per te e per la tua casa; e quando la vita cambia, puoi completare il divano e farlo cambiare con te.",90,256,1139.00,6,null,null,null,"Hallarp grigio","Beige",null,null,null),
                         ("P0046","DivanoPoltrona","Poltrona Reclinabile","Poltrona reclinabile fino a 180°. Movimento alzapersona e reclinabile (pediera e schienale) a 2 motori.",104,90,799.00,6,null,null,null,"Daytona","3 Panna",null,null,null),
                         ("C0047","CuscinoGuanciale","Guanciale Memory Wave H11","Guanciale Made in Italy in memory. Federa esterna in Probiotic con probiotici attivi dalle proprietà anallergiche. La sua particolare forma lo rende ideale per chi dorme supino e offre un maggiore sostegno al collo. Altezza: parte bassa 9 cm; parte alta 11 cm circa.",80,30,59,3,null,null,null,null,null,"Probiotic","Fibre sintetiche","Rettangolare"),
                         ("C0048","CuscinoGuanciale","Guanciale Piuma Basic H12","Guanciale Made in Italy con imbottitura 80% piccole piume d’oca e 15% piuma d’oca.",80,50,25,3,null,null,null,null,null,"Piume d'oca","Basic","Rettangolare"),
                         ("MPM49","MaterassoPiazzaEMezzo","Attivo Plus","Il nuovo Attivo Plus è il materasso che abbina l’elevato sostegno delle 800 molle indipendenti insacchettate ad una gradevole sensazione di accoglienza originata dal Memory Fresh inserito nel pannello superiore. Le molle indipendenti favoriscono una migliore distribuzione del peso corporeo, pur non venendo meno la robustezza ed il sostegno complessivo del prodotto",120,190,500.00,5,"Molla",null,null,null,null,null,null,null),
                         ("MS050","MaterassoSingolo","Infinity","Infinity è il materasso ortopedico che grazie al suo particolare molleggio di ben 700 molle è in grado di sostenere e offrire un supporto solido e duraturo nel tempo anche a persone dal peso elevato.", 200,90, 310.00,5,"Molla",null,null,null,null,null,null,null),
                         ("LPM51","LettoPiazzaEMezzo","Cloe","Struttura letto contenitore. Testata imbottita con falde laterali e sagomata grazie all'applicazione di bottoni ton sur ton; una forma che avvolge e definisce conferendo carattere a tutto il letto. Ci si può sbizzarrire nella scelta dei colori (oltre 220) che, vista la struttura del letto lineare e discreta, possono essere brillanti, vivaci o come ci suggerisce la fantasia. Rete a doghe inclusa nel prezzo.",160,190,1068,10,null,"Albaclara",null,null,null,null,null,null),
                         ("LPM52","LettoPiazzaEMezzo","Dot H20","Struttura letto con contenitore. Testata con 2 cuscini e 6 bottoni (cuscino unico nelle versioni piazza e mezza e singolo). Rete a doghe inclusa nel prezzo. Sollevamento del contenitore a compasso. Contenitore non disponibile con rete elettrica. Il letto Dot 20 è disponibile nelle tipologie: matrimoniale, king size, singola, piazza e mezza e king size.",160,190,898.00,7,null,"Bordeaux",null,null,null,null,null,null),
                         ("DC053","DivanoContenitore","Kevin","Ideato per essere il massimo del comfort, il modello Kevin ha uno schienale caratterizzato da cuscini regolabili e una penisola per un relax totale. Inoltre, in pochissimi gesti, può diventare un ampio e comodo letto.",280,165,930.00,5,null,null,null,"non sfoderabile","bianco",null,null,null),
                         ("DC054","DivanoContenitore","Emilia","Il divano letto modello EMILIA  con penisola e pouf estraibile, che, all'occorrenza diventa un pratico piano di riposo, è un divano dalle linee moderne impreziosite da eleganti trame trapuntate sui larghi e morbidi braccioli.",278,175,1099.00,6,null,null,null,"Enjoy-Lux","vaniglia",null,null,null);