import { safeReturnUrl } from './return-url';

describe('safeReturnUrl', () => {
  it('keeps in-app paths, including query strings', () => {
    expect(safeReturnUrl('/search?q=dune')).toBe('/search?q=dune');
  });

  it('falls back to the library when nothing is given', () => {
    expect(safeReturnUrl(undefined)).toBe('/library');
    expect(safeReturnUrl('')).toBe('/library');
  });

  it('rejects absolute and protocol-relative URLs that would leave the app', () => {
    expect(safeReturnUrl('https://evil.example')).toBe('/library');
    expect(safeReturnUrl('//evil.example')).toBe('/library');
    expect(safeReturnUrl('/\\evil.example')).toBe('/library');
  });

  it('never sends the user back to the auth pages', () => {
    expect(safeReturnUrl('/login')).toBe('/library');
    expect(safeReturnUrl('/register')).toBe('/library');
  });
});
