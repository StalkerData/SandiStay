// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	namespace App {
		// interface Error {}
		
		// Definimos las variables globales que inyecta nuestro hooks.server.ts
		interface Locals {
			token?: string;
			userRole?: string;
		}
		
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
}

export {};
