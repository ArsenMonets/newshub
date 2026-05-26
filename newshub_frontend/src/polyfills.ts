/**
 * Polyfills for browser compatibility
 * Fixes issue with sockjs-client trying to access 'global' variable in browser environment
 */

declare var global: any;

// Ensure 'global' is available in the browser (required for sockjs-client and other legacy libraries)
if (typeof global === 'undefined') {
  (window as any).global = window;
}
