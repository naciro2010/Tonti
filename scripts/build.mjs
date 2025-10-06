import { build } from "esbuild";
import { cp, mkdir, readFile, rm, writeFile } from "node:fs/promises";
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

await build({
  entryPoints: [entryPoint],
  bundle: true,
  format: "esm",
  outfile: path.join(assetsDir, "app.js"),
  sourcemap: true,
  minify: true,
  target: "es2020",
  define: {
    "process.env.NODE_ENV": '"production"'
  },
  alias: {
    "@core": path.resolve("src/core"),
    "@data": path.resolve("src/data"),
    "@lib": path.resolve("src/lib"),
    "@ui": path.resolve("src/ui"),
    "@views": path.resolve("src/views")
  }
});

await cp(cssSource, path.join(assetsDir, "app.css"));

try {
  await cp("public", outDir, { recursive: true });
} catch (error) {
  if (error && typeof error === "object" && "code" in error && error.code !== "ENOENT") {
    throw error;
  }
}

await writeHtml();

console.log("Build complete ➜", outDir);
