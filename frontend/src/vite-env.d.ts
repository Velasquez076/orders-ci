/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL of the Orders API. Empty means same origin (dev proxy / nginx). */
  readonly VITE_API_BASE_URL?: string;
}
