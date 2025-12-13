import {useEffect, useState} from "react";
import styles from "./home.module.css";

type Entry = { name: string, plainText: boolean, content: string, createdAt: number, id: number }

export function Home() {
    let [items, setItems] = useState<Entry[]>([

        // {name: "first", plainText: true, content:"testing content here", createdAt: 1000, id:18183738},
        // {name: "second test", plainText: true, content:"Inventore reprehenderit consequatur velit qui totam. Occaecati sint voluptatem amet nobis repellendus reiciendis. Qui volup", createdAt: 1000, id:18183738},
        // {name: "a file", plainText: false, content:"File", createdAt: 2000, id:181836738},
        // {name: "long filename testing  123123121928273.png", plainText: false, content:"File", createdAt: 2000, id:181836738}

    ]);
    // @ts-ignore
    let [status, setStatus] = useState("");
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
            setStatus("Crypto not available! Are you using HTTPS?")
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
            <GreetingForm fetchItems={fetchItems} setStatus={setStatus} encryptionKey={encryptionKey}/>
            <EncryptionStatus encryptionKey={encryptionKey} setEncryptionKey={setEncryptionKey}/>
        </div>
    } else {
        inner = <>
            <EncryptionStatus encryptionKey={encryptionKey} setEncryptionKey={setEncryptionKey}/>
        <div className={styles.mainFlex}>
            <GreetingForm fetchItems={fetchItems} setStatus={setStatus} encryptionKey={encryptionKey}/>
        </div>

        </>
    }
    return <div className={styles.dataBackground + " " + styles.main}>
        {inner}


        <h2>Last Update: {secondsSinceUpdate + "s"}</h2>
        <h2 className={styles.error}>{status}</h2>
        <br/>
        <ol>
            {items.map((item: Entry, index) => (
                <li className={styles.dataItem} key={index}>
                    <div className={styles.itemFormat}>
                        {formatEntry(item)}
                    </div>
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
                            <summary>{trimToFit(entry.content, 30)}</summary>
                            <p>{entry.content}</p>
                        </details>
                    ) : entry.content}
                    <button onClick={() => copyToClipboard(entry.content)} className={styles.copyButton}>Copy</button>

                </>
        } else {
            const created = String(entry.createdAt)
            const fileNameBase64 = btoa(entry.name)

            entryText = <button type="button" className={styles.dataButton} onClick={async () => {
                const url = await getDownloadUrl(created, fileNameBase64) as string
                const a = document.createElement("a")
                a.href = url
                a.download = entry.name
                a.click()

            }}>{"Download " + trimToFit(entry.name, 10)}</button>

            if (isImage(entry.name)) {

                entryText = <><button className={styles.dataButton + " " + styles.bobby} id={"imageButton_" + entry.id + entry.createdAt} onClick={async () => {
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
            const res = await fetch("api/downloadfile" + "?created=" + created + "&filename=" + fileNameBase64)

            const bytes = await res.bytes()

            const key = await getEncryptionKey(encryptionKey)

            const blob = new Blob([await decryptData(bytes.buffer, key)], { type: "application/octet-stream" });
            return URL.createObjectURL(blob)
        } catch (e: any) {
            console.error(e)
            setStatus(e.message)
        }

    }

    function isImage(name: string): boolean {
        return images.some(s => name.endsWith(s))
    }

    async function deleteItem(entry: Entry) {
        if (!confirm("Confirm deletion?")) {
            return
        }
        const response = await fetch("api/delete" + "?created=" + String(entry.createdAt) + "&id=" + entry.id, {
            method: "DELETE"
        })
        if (!response.ok) {
            setStatus("Error deleting item " + entry)
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
            const res = await fetch("api/entries");
            if (!res.ok) {
                setStatus("Error getting entries" + res.status)
                return
            }
            const entries = await res.json();
            setSecondsSinceUpdate(0);

            if (!entries || !encryptionKey) {
                setStatus("fetch was ok but error getting entries anyway.")
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
            setStatus("Error fetching items. Check the console")
            console.log(err);
        }
    }
}

function trimToFit(str: string, len: number) {
    if (str.length < len) {
        return str;
    }
    return str.substring(0, len) + "..."
}

// @ts-ignore
function GreetingForm({ fetchItems, setStatus, encryptionKey}) {
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

        const body = new FormData()

        if (text) {
            body.append("text", await encryptDataToBase64(new TextEncoder().encode(text), key))
        }

        if (files[0].size !== 0) {
            setStatus("Encrypting...")
            const fileArrays = await Promise.all(
                files.map(async (f) => new Uint8Array(await f.arrayBuffer()))
            )

            for (let i = 0; i < files.length; i++) {
                let buf;
                try {
                    buf = await encryptData(fileArrays[i], key)
                } catch (e: any) {
                    setStatus(e.message)
                    throw e;
                    return
                }
                const blob = new Blob([buf], {type: "application/octet-stream"})
                body.append("file", blob, files[i].name)
            }
        }


        const response = await fetch("api/submit", {
            method: 'POST',
            body: body
        });



        if (!response.ok) {
            console.log("not ok " + response.status)
            setStatus(await response.text() + " " + String(response.status));
            return;
        }
        setStatus("")

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
        { name: "AES-CTR", length: 256 },
        false,
        ["encrypt", "decrypt"]
    );
}

async function encryptDataToBase64(data: ArrayBuffer, key: CryptoKey): Promise<string> {
    const arrayBuffer = await encryptData(data, key)
    const uint8Array = new Uint8Array(arrayBuffer);

    let string = ""

    for (let i = 0; i < uint8Array.length; i+= 30000) {
        const slice = uint8Array.subarray(i, Math.min(i+30000, uint8Array.length))
        string+= String.fromCharCode(...slice)
    }
    return btoa(string);
}

async function decryptData(encryptedBuffer: ArrayBuffer, key: CryptoKey): Promise<ArrayBuffer> {
    const encryptedArray = new Uint8Array(encryptedBuffer);

    const iv = encryptedArray.slice(0, 16);
    const data = encryptedArray.slice(16);


    let datas = []

    for (let i = 0; i < data.byteLength; i+= 128_000_000) {
        const encryptedData = await crypto.subtle.encrypt(
            { name: "AES-CTR", counter: iv, length: 64 },
            key,
            data.slice(i, i+128_000_000)
        );
        iv[iv.length-1]++

        datas.push(encryptedData)
    }

    const result = new Uint8Array(data.byteLength)
    let prev = 0

    for (const chunk of datas) {
        result.set(new Uint8Array(chunk), prev)
        prev+= chunk.byteLength
    }

    return result.buffer;
}

async function encryptData(data: ArrayBuffer, key: CryptoKey): Promise<ArrayBuffer> {
    const iv = crypto.getRandomValues(new Uint8Array(16));

    // const numChunks = Math.floor(1 + data.byteLength / 128_000_000) // 1MB per chunk

    let datas = []

    const staleIV = new Uint8Array(iv);

    for (let i = 0; i < data.byteLength; i+= 128_000_000) {
        const encryptedData = await crypto.subtle.encrypt(
            { name: "AES-CTR", counter: iv, length: 64 },
            key,
            data.slice(i, i+128_000_000)
        );
        iv[iv.length-1]++

        datas.push(encryptedData)
    }

    const result = new Uint8Array(iv.length + data.byteLength)
    result.set(staleIV, 0);
    let prev = iv.length

    for (const chunk of datas) {
        result.set(new Uint8Array(chunk), prev)
        prev+= chunk.byteLength
    }

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
