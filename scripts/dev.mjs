import { context } from "esbuild";
import { cp, mkdir, readFile, rm, writeFile } from "node:fs/promises";
import { watch } from "node:fs";
import path from "node:path";

const outDir = "dist";
const assetsDir = path.join(outDir, "assets");
const entryPoint = "src/app.ts";
const cssSource = "src/styles/app.css";

await rm(outDir, { recursive: true, force: true });
await mkdir(assetsDir, { recursive: true });

async function writeHtml() {
  const html = await readFile("index.html", "utf-8");
  const finalHtml = html
    .replace("/src/app.ts", "/assets/app.js")
    .replace("/src/styles/app.css", "/assets/app.css");
  await writeFile(path.join(outDir, "index.html"), finalHtml);
}

async function copyCss() {
  await cp(cssSource, path.join(assetsDir, "app.css"));
}

await writeHtml();
await copyCss();
try {
  await cp("public", outDir, { recursive: true });
} catch (error) {
  if (error && typeof error === "object" && "code" in error && error.code !== "ENOENT") {
    throw error;
  }
}

const cssWatcher = watch(cssSource, { persistent: true }, async (eventType) => {
  if (eventType === "change" || eventType === "rename") {
    try {
      await copyCss();
      console.log("✔ CSS updated");
    } catch (error) {
      console.error("✖ CSS copy failed", error);
    }
  }
});

const ctx = await context({
  entryPoints: [entryPoint],
  bundle: true,
  format: "esm",
  outfile: path.join(assetsDir, "app.js"),
  sourcemap: true,
  target: "es2020",
  define: {
    "process.env.NODE_ENV": '"development"'
  },
  alias: {
    "@core": path.resolve("src/core"),
    "@data": path.resolve("src/data"),
    "@lib": path.resolve("src/lib"),
    "@ui": path.resolve("src/ui"),
    "@views": path.resolve("src/views")
  }
});

await ctx.watch();
const server = await ctx.serve({ servedir: outDir, host: "0.0.0.0", port: 4173 });

console.log(`Dev server ready on http://localhost:${server.port}`);

const shutdown = async () => {
  cssWatcher.close();
  await ctx.dispose();
  process.exit(0);
};

process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);
