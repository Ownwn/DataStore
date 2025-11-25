import {useEffect, useState} from "react";
import styles from "./home.module.css";

type Entry = { name: string, plainText: boolean, content: string, createdAt: number, id: number }

export function Home() {
    let [items, setItems] = useState<Entry[]>([]);
    // @ts-ignore
    let [error, setError] = useState("");
    let [secondsSinceUpdate, setSecondsSinceUpdate] = useState(0)
    let [encryptionKey, setEncryptionKey] = useState("")

    const images = ["jpg", "jpeg", "png", "webp", "gif", "svg"]

    useEffect(() => {
        const interval = setInterval(() => {
            setSecondsSinceUpdate(prev => prev + 1);
        }, 1000);

        return () => clearInterval(interval)
    }, []);

    useEffect(() => {
        if (encryptionKey.length !== 0) {
            document.cookie = "encodedEncryption=" + String(encryptionKey) + "; max-age=" + String(60*60*24*365)
            fetchItems()
        }
    }, [encryptionKey]);

    useEffect(() => {
        if (crypto.subtle === undefined) {
            setError("Crypto not available! Are you using HTTPS?")
        }

        const encryption = document.cookie
            .split("; ")
            .find((row) => row.startsWith("encodedEncryption="))
            ?.split("=")[1];
        if (encryption !== undefined) {
            setEncryptionKey(encryption)
        }
    }, []);

    let inner;
    if (encryptionKey.length === 0) {
        inner = <div className={styles.mainFlex + " " + styles.padding}>
            <GreetingForm fetchItems={fetchItems} setError={setError} encryptionKey={encryptionKey}/>
            <EncryptionStatus encryptionKey={encryptionKey} setEncryptionKey={setEncryptionKey}/>
        </div>
    } else {
        inner = <>
            <EncryptionStatus encryptionKey={encryptionKey} setEncryptionKey={setEncryptionKey}/>
        <div className={styles.mainFlex}>
            <GreetingForm fetchItems={fetchItems} setError={setError} encryptionKey={encryptionKey}/>
        </div>

        </>
    }
    return <div className={styles.dataBackground + " " + styles.main}>
        {inner}


        <h2>Last Update: {secondsSinceUpdate + "s"}</h2>
        <h2 className={styles.error}>{error}</h2>
        <br/>
        <ol>
            {items.map((item: Entry, index) => (
                <li className={styles.dataItem} key={index}>
                    {formatEntry(item)}
                </li>
            ))}
        </ol>
    </div>;

    function formatEntry(entry: Entry)  {
        let entryText;

        if (entry.plainText) {
            entryText =
                <>
                    {entry.content.length >= 30 ? (
                        <details>
                            <summary>{entry.content.substring(0, 30) + "..."}</summary>
                            <p>{entry.content}</p>
                        </details>
                    ) : entry.content}
                    <button onClick={() => copyToClipboard(entry.content)} className={styles.dataButton}>Copy</button>

                </>
        } else {
            const created = String(entry.createdAt)
            const fileNameBase64 = btoa(entry.name)

            entryText = <button type="button" onClick={async () => {
                const url = await getDownloadUrl(created, fileNameBase64) as string
                const a = document.createElement("a")
                a.href = url
                a.download = entry.name
                a.click()

            }}>{entry.name}</button>

            if (isImage(entry.name)) {

                entryText = <><button id={"imageButton_" + entry.id + entry.createdAt} onClick={async () => {
                    const url = await getDownloadUrl(created, fileNameBase64) as string
                    const img = document.createElement("img")

                    img.src = url
                    img.alt = "image for " + entry.name
                    img.width = 500
                    img.height = 500

                    document.getElementById("imageButton_" + entry.id + entry.createdAt)?.replaceWith(img)
                }}>View</button><>{entryText}</></>
            }

        }
        return <>
        {entryText}
        <span><button onClick={() => deleteItem(entry)} className={styles.deleteButton}>Delete</button></span>
        </>

    }

    async function getDownloadUrl(created: string, fileNameBase64: string) {
        try {
            const res = await fetch("downloadfile" + "?created=" + created + "&filename=" + fileNameBase64)
            const text = await res.text()

            const key = await getEncryptionKey(encryptionKey)

            const buffer = base64ToArrayBuffer(text)

            const blob = new Blob([await decryptData(buffer, key)], { type: "application/octet-stream" });
            return URL.createObjectURL(blob)
        } catch (e: any) {
            console.error(e)
            setError(e.message)
        }

    }

    function isImage(name: string): boolean {
        return images.some(s => name.endsWith(s))
    }

    async function deleteItem(entry: Entry) {
        if (!confirm("Confirm deletion?")) {
            return
        }
        const response = await fetch("delete" + "?created=" + String(entry.createdAt) + "&id=" + entry.id, {
            method: "DELETE"
        })
        if (!response.ok) {
            setError("Error deleting item " + entry)
            return
        }
        await fetchItems()

    }

    async function copyToClipboard(text: string) {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(text);
        } else { // thanks safari for not playing nice!!
            const textarea = document.createElement('textarea');
            textarea.value = text;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
        }

    }


    async function fetchItems() {
        try {
            const res = await fetch("entries");
            if (!res.ok) {
                setError("Error getting entries" + res.status)
                return
            }
            const entries = await res.json();
            setSecondsSinceUpdate(0);

            if (!entries || !encryptionKey) {
                setError("fetch was ok but error getting entries anyway.")
                return
            }

            const key = await getEncryptionKey(encryptionKey);
            const decryptedEntries = await Promise.all(
                entries.map(async (entry: Entry) => {
                    if (!entry.plainText) {
                        return entry
                    }

                    try {
                        const encryptedBuffer = base64ToArrayBuffer(entry.content);
                        const decryptedBuffer = await decryptData(encryptedBuffer, key);
                        return { ...entry, content: new TextDecoder().decode(decryptedBuffer) };
                    } catch (err) {
                        console.error("Error decrypting", err);
                        return { ...entry, content: "???" };
                    }
                })
            );
            setItems(decryptedEntries.sort((a: Entry, b: Entry) => b.createdAt - a.createdAt));


        } catch (err) {
            setError("Error fetching items. Check the console")
            console.log(err);
        }
    }
}


// @ts-ignore
function GreetingForm({ fetchItems, setError, encryptionKey}) {
    const handleSubmit = async (e: any) => {
        e.preventDefault();
        const formData = new FormData(e.target);

        const files = formData.getAll("files") as File[];
        const text = formData.get("data") as string;
        // @ts-ignore
        if (files[0].size === 0 && !text) {
            return;
        }

        const key = await getEncryptionKey(encryptionKey)

        let body = [{}]

        if (text) {
            body.push({"type": "text", "text": await encryptDataToBase64(text as string, key)})
        }

        if (files[0].size !== 0) {
            const fileArrays = await Promise.all(
                files.map(async (f) => new Uint8Array(await f.arrayBuffer()))
            )

            for (let i = 0; i < files.length; i++) {
                const encryptedFileBytes = await encryptDataToBase64(fileArrays[i], await getEncryptionKey(encryptionKey))
                body.push({"type": "file", "file": encryptedFileBytes.toString(), "filename": files[i].name})
            }
        }


        const response = await fetch("submit", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body)
        });



        if (!response.ok) {
            console.log("not ok " + response.status)
            setError(await response.text() + " " + String(response.status));
            return;
        }

        const result = await response.text();
        console.log(result);

        e.target.reset()
        fetchItems();
    };

    return (
        <form onSubmit={handleSubmit}>
            <textarea name="data" placeholder="Data to upload"/>
            <br/>
            <input type="file" multiple name="files" className={styles.fileInput}/>
            <br/>
            <button type="submit">Submit</button>
        </form>
    );
}

// @ts-ignore
function EncryptionStatus({encryptionKey, setEncryptionKey}) {
    return <>
        <div className={styles.encryptionBox}>
            <p>{encryptionKey.length === 0 ? "Please set your encryption key below" : "Clear your cookies to change your encryption key"}</p>
            <br/>
            {encryptionKey.length !== 0 ? <></> :
                <>
                    <button type="button" onClick={() => setEncryptionKey(getValue())}>Set key</button>
                    <input type="password" id="encryptioninput" placeholder="Encryption Key" className={styles.keyBox}/>
                </>
            }

        </div>
    </>
}

function getValue() {
    // @ts-ignore
    const value = document.getElementById("encryptioninput")?.value
    if (!value) return "";
    return value
}


/** Below here is AI slop. Crypto's gotta be the safest thing to vibe code right? */

async function getEncryptionKey(password: string): Promise<CryptoKey> {
    const encoder = new TextEncoder();
    const keyMaterial = await crypto.subtle.importKey(
        "raw",
        encoder.encode(password),
        "PBKDF2",
        false,
        ["deriveBits", "deriveKey"]
    );

    return await crypto.subtle.deriveKey(
        {
            name: "PBKDF2",
            salt: encoder.encode("unique-salt"),
            iterations: 100000,
            hash: "SHA-256"
        },
        keyMaterial,
        { name: "AES-GCM", length: 256 },
        false,
        ["encrypt", "decrypt"]
    );
}

async function decryptData(encryptedBuffer: ArrayBuffer, key: CryptoKey): Promise<ArrayBuffer> {
    const encryptedArray = new Uint8Array(encryptedBuffer);

    const iv = encryptedArray.slice(0, 12);
    const encryptedData = encryptedArray.slice(12);

    return await crypto.subtle.decrypt(
        { name: "AES-GCM", iv },
        key,
        encryptedData
    );
}


async function encryptDataToBase64(data: string | ArrayBuffer, key: CryptoKey): Promise<string> {
    const arrayBuffer = await encryptData(data, key);
    const uint8Array = new Uint8Array(arrayBuffer);

    let string = ""

    for (let i = 0; i < uint8Array.length; i+= 30000) {
        const slice = uint8Array.subarray(i, Math.min(i+30000, uint8Array.length))
        string+= String.fromCharCode(...slice)
    }
    return btoa(string);
}

async function encryptData(data: string | ArrayBuffer, key: CryptoKey): Promise<ArrayBuffer> {
    const encoder = new TextEncoder();
    const dataBuffer = typeof data === "string" ? encoder.encode(data) : data;

    const iv = crypto.getRandomValues(new Uint8Array(12));
    const encryptedData = await crypto.subtle.encrypt(
        { name: "AES-GCM", iv },
        key,
        dataBuffer
    );

    const result = new Uint8Array(iv.length + encryptedData.byteLength);
    result.set(iv, 0);
    result.set(new Uint8Array(encryptedData), iv.length);

    return result.buffer;
}


function base64ToArrayBuffer(base64: string): ArrayBuffer {
    const binaryString = atob(base64);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }
    return bytes.buffer;
}
