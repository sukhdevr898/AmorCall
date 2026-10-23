const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-122d9889-5e2a-41c7-bfb5-b3253024ef74";

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: "127.0.0.1",
      port: 8085,
      rules: fs.readFileSync("firestore.rules", "utf8"),
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore({ databaseId: DATABASE_ID });
  const userRef = unauthDb.collection("users").doc("user_123");
  await assertFails(userRef.get());
  await assertFails(userRef.set({
    userId: "user_123",
    displayName: "Duora User",
    partnerCode: "DUORA-12345",
    createdAt: new Date(),
    updatedAt: new Date(),
  }));
});

test("Authenticated user can write their own profile", async () => {
  const authDb = testEnv.authenticatedContext("user_123").firestore({ databaseId: DATABASE_ID });
  const userRef = authDb.collection("users").doc("user_123");
  await assertSucceeds(userRef.set({
    userId: "user_123",
    displayName: "Duora User",
    partnerCode: "DUORA-12345",
    createdAt: new Date(),
    updatedAt: new Date(),
  }));
});

test("Authenticated user cannot write another user's profile", async () => {
  const authDb = testEnv.authenticatedContext("user_123").firestore({ databaseId: DATABASE_ID });
  const userRef = authDb.collection("users").doc("user_456");
  await assertFails(userRef.set({
    userId: "user_456",
    displayName: "Fake User",
    partnerCode: "DUORA-45678",
    createdAt: new Date(),
    updatedAt: new Date(),
  }));
});
