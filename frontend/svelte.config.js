import adapter from '@sveltejs/adapter-node';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	preprocess: vitePreprocess(),

	kit: {
		adapter: adapter(),
		// FIX PARA NGINX: Apaga la validación de origen del formulario
		csrf: {
			checkOrigin: false
		}
	}
};

export default config;
