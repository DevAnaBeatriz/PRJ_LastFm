const express = require("express");
const app = express();
const handlebars = require("express-handlebars").engine;
const bodyParser = require("body-parser");
const cookieParser = require("cookie-parser"); 
const { initializeApp, cert } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getAuth } = require("firebase-admin/auth");
const serviceAccount = require('./GERARCHAVE.json')
const axios = require("axios");

initializeApp({
  credential: cert(serviceAccount),
});

const db = getFirestore();
const auth = getAuth();

app.engine("handlebars", handlebars({ defaultLayout: "main" }));
app.set("view engine", "handlebars");

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(cookieParser()); 
app.use(express.static("public"));

function checkAuth(req, res, next) {
  const sessionCookie = req.cookies.session || ""; 
  auth
    .verifySessionCookie(sessionCookie, true)
    .then((decodedClaims) => {
      req.user = decodedClaims; 
      next();
    })
    .catch(() => {
      req.user = null; 
      next();
    });
}

app.get("/", checkAuth, async (req, res) => {
  if (req.user) {
    try {
      const scrobblesSnapshot = await db
        .collection("scrobbles")
        .where("userId", "!=", req.user.uid) 
        .get();

      const scrobbles = [];
      scrobblesSnapshot.forEach((doc) => {
        scrobbles.push({
          id: doc.id,
          ...doc.data(), 
        });
      });
      res.render("index", {
        scrobbles,
        user: req.user,
      });
    } catch (error) {
      console.error("Erro ao buscar scrobbles:", error);
      res.status(500).send("Erro ao carregar scrobbles.");
    }
  } else {
    res.redirect("/login");
  }
});


app.get("/login", (req, res) => {
  res.render("login");
});

app.post("/login", async (req, res) => {
  const { email, password } = req.body;

  try {
    const response = await axios.post(
      "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword",
      {
        email,
        password,
        returnSecureToken: true,
      },
      {
        params: {
          key: "CHAVE DO PROJETO FIREBASE",
        },
      }
    );

    const idToken = response.data.idToken;

    const sessionCookie = await auth.createSessionCookie(idToken, {
      expiresIn: 60 * 60 * 24 * 5 * 1000,  
    });

    res.cookie("session", sessionCookie, {
      httpOnly: true,
      secure: true,
    });

    res.redirect("/");
  } catch (error) {
    console.error("Erro ao autenticar usuário:", error.response?.data || error.message);
    res.redirect("/login"); 
  }
});

app.get("/register", checkAuth, async (req, res) => {
  if (req.user) {
    res.redirect("/")
  } else {
    res.render("register");
  }
});

app.post("/register", async (req, res) => {
  const { nome, email, password } = req.body;

  try {
    const userRecord = await auth.createUser({ email, password });
    console.log("Usuário criado:", userRecord.uid);
    var result = db.collection('usuarios').add({
      nome: nome,
      email: email
    }).then(function () {
      console.log('Usuário Cadastrado!');
      res.redirect('/login')
    })
  } catch (error) {
    console.log("Erro ao criar usuário:", error);
    res.redirect("/register");
  }
});

app.get("/logout", (req, res) => {
  res.clearCookie("session");
  res.redirect("/login");
});

app.get("/search", async (req, res) => {
  try {
    const query = req.query.q; 
    const scrobblesRef = db.collection("scrobbles");
    const scrobblesSnapshot = await scrobblesRef
      .where("title", ">=", query) 
      .where("title", "<=", query + "\uf8ff")
      .get();

    const scrobbles = [];
    scrobblesSnapshot.forEach((doc) => {
      scrobbles.push({ id: doc.id, ...doc.data() });
    });

    res.json({ scrobbles });
  } catch (error) {
    console.error("Erro ao buscar scrobbles:", error);
    res.status(500).json({ error: "Erro ao buscar scrobbles" });
  }
});

app.get("/view_scrobble/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const scrobbleId = req.params.id;

    try {
      const doc = await db.collection('scrobbles').doc(scrobbleId).get();

      if (!doc.exists) {
        return res.status(404).send("scrobble não encontrada");
      }

      const scrobbleData = doc.data();

      const genero = scrobbleData.genero.split(',').map(ingredient => ingredient.trim());

      res.render("view_scrobble", {
        scrobble: { id: doc.id, ...scrobbleData, genero }
      });
    } catch (error) {
      console.log("Erro ao recuperar scrobble:", error);
      res.status(500).send("Erro ao recuperar scrobble");
    }
  } else {
    res.redirect("/login");
  }
});

app.get("/scrobbles", checkAuth, async (req, res) => {
  if (req.user) {
    try {
      const scrobblesSnapshot = await db
        .collection("scrobbles")
        .where("userId", "==", req.user.uid) 
        .get();

      const scrobbles = [];
      scrobblesSnapshot.forEach((doc) => {
        scrobbles.push({
          id: doc.id,
          ...doc.data(), 
        });
      });

      res.render("scrobbles", {
        scrobbles,
        user: req.user,
      });
    } catch (error) {
      console.error("Erro ao buscar scrobbles:", error);
      res.status(500).send("Erro ao carregar scrobbles.");
    }
  } else {
    res.redirect("/login");
  }
})

app.get("/add_scrobble", checkAuth, async (req, res) => {
  res.render("add_scrobble")
})

app.post("/add_scrobble", checkAuth, async (req, res) => {
  if (req.user) {
    const title = req.body.title;
    const genero = req.body.genero;
    const info = req.body.info;

    try {

      const email = req.user.email;

      const userSnapshot = await db.collection("usuarios").where("email", "==", email).get();

      if (userSnapshot.empty) {
        return res.status(400).send("Usuário não encontrado no Firestore.");
      }

      const userDoc = userSnapshot.docs[0];
      const username = userDoc.exists ? userDoc.data().nome : "Usuário Anônimo"; 
      const scrobbleRef = db.collection("scrobbles").doc();
      const scrobbleId = scrobbleRef.id;

      await scrobbleRef.set({
        id: scrobbleId, 
        title: title,
        genero: genero,
        info: info,
        userId: req.user.uid, 
        user: username, 
      });

      console.log("scrobble adicionada com sucesso!");
      res.redirect("/scrobbles"); 
    } catch (error) {
      console.error("Erro ao adicionar scrobble:", error.message);
      res.status(400).send("Erro ao adicionar scrobble: " + error.message);
    }
  } else {
    res.redirect("/login"); 
  }
});


app.get("/edit_scrobble/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const scrobbleId = req.params.id;

    try {
      const doc = await db.collection('scrobbles').doc(scrobbleId).get();

      if (!doc.exists) {
        return res.status(404).send("scrobble não encontrada");
      }

      const scrobbleData = doc.data();

      const genero = scrobbleData.genero.split(',').map(ingredient => ingredient.trim());

      res.render("edit_scrobble", {
        scrobble: { id: doc.id, ...scrobbleData, genero }
      });
    } catch (error) {
      console.log("Erro ao recuperar scrobble:", error);
      res.status(500).send("Erro ao recuperar scrobble");
    }
  } else {
    res.redirect("/login")
  }
})

app.post("/edit_scrobble/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const scrobbleId = req.params.id;
    const { title, genero, info } = req.body;

    try {
      const scrobbleRef = db.collection('scrobbles').doc(scrobbleId);
      const scrobbleDoc = await scrobbleRef.get();
      if (!scrobbleDoc.exists) {
        return res.status(404).send("scrobble não encontrada");
      }
      const updateData = {};
      if (title) updateData.title = title;
      if (genero) updateData.genero = genero;
      if (info) updateData.info = info;
      await scrobbleRef.update(updateData);

      console.log("scrobble atualizada com sucesso!");
      res.redirect("/"); 
    } catch (error) {
      console.error("Erro ao atualizar scrobble:", error.message);
      res.status(400).send("Erro ao atualizar scrobble: " + error.message);
    }
  } else {
    res.redirect("/login"); 
  }
});

app.get("/manage_scrobble/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const scrobbleId = req.params.id;

    try {
      const doc = await db.collection('scrobbles').doc(scrobbleId).get();

      if (!doc.exists) {
        return res.status(404).send("scrobble não encontrado");
      }

      const scrobbleData = doc.data();
      const genero = scrobbleData.genero.split(',').map(ingredient => ingredient.trim());
      res.render("manage_scrobble", {
        scrobble: { id: doc.id, ...scrobbleData, genero }
      });
    } catch (error) {
      console.log("Erro ao recuperar scrobble:", error);
      res.status(500).send("Erro ao recuperar scrobble");
    }
  } else {
    res.redirect("/login")
  }
})

app.get("/sobre", checkAuth, async (req, res) => {
  if (req.user) {
    res.render("sobre")
  } else {
    res.redirect("/login")
  }
})

app.get("/delete_scrobble/:id", checkAuth, async (req, res) => {
  if (req.user) {
    const scrobbleId = req.params.id;

    try {
      const scrobbleRef = db.collection('scrobbles').doc(scrobbleId);
      const scrobbleDoc = await scrobbleRef.get();

      if (!scrobbleDoc.exists) {
        return res.status(404).send("scrobble não encontrada");
      }
      await scrobbleRef.delete();

      console.log("scrobble excluída com sucesso!");
      res.redirect("/");
    } catch (error) {
      console.error("Erro ao excluir scrobble:", error.message);
      res.status(400).send("Erro ao excluir scrobble: " + error.message);
    }
  } else {
    res.redirect("/login"); 
  }
});


app.listen(8081, () => {
  console.log("O Servidor está ativo e rodando na porta http://localhost:8081!");
});
